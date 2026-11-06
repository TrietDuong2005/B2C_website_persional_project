package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.CategoryResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.shop.*;
import com.dahdt.ecommerce.modules.catalog.entity.*;
import com.dahdt.ecommerce.modules.catalog.mapper.BrandMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.CategoryMapper;
import com.dahdt.ecommerce.modules.catalog.repository.*;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.fulfillment.OnlineAvailabilityService;
import com.dahdt.ecommerce.modules.store.entity.Store;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ truy vấn danh mục và sản phẩm cho khách hàng trên Shop Online.
 * Chỉ đọc (read-only), tối ưu hóa hoàn toàn chống N+1 queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopCatalogServiceImpl implements ShopCatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final OnlineAvailabilityService onlineAvailabilityService;

    @Override
    public Page<ShopProductItemDTO> listProducts(ShopProductFilterDTO filter, Pageable pageable) {
        if (filter == null) {
            filter = new ShopProductFilterDTO();
        }

        Specification<Product> spec = buildProductSpecification(filter);

        // Chạy truy vấn phân trang sản phẩm với Spring Data JPA Specification
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> productPage = productRepository.findAll(spec, pageRequest);

        List<Product> products = productPage.getContent();
        if (products.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, productPage.getTotalElements());
        }

        // Tối ưu chống N+1: nạp biến thể và tồn kho theo lô (Batch fetching)
        List<ShopProductItemDTO> items = enrichProducts(products);
        return new PageImpl<>(items, pageable, productPage.getTotalElements());
    }

    @Override
    public ShopFilterDataDTO getFilterData() {
        // 1. Cây danh mục đang hoạt động (gốc kèm con)
        List<Category> rootCats = categoryRepository.findActiveRootCategoriesWithChildren();
        List<CategoryResponseDTO> categoryDtos = rootCats.stream().map(c -> {
            CategoryResponseDTO dto = categoryMapper.toDto(c);
            if (c.getChildren() != null) {
                List<CategoryResponseDTO> children = c.getChildren().stream()
                        .filter(Category::getIsActive)
                        .map(categoryMapper::toDto)
                        .toList();
                dto.setChildren(children);
            }
            return dto;
        }).toList();

        // 2. Danh sách thương hiệu đang hoạt động có sản phẩm
        List<Brand> brands = brandRepository.findActiveBrandsWithActiveProducts();
        List<BrandResponseDTO> brandDtos = brandMapper.toDtoList(brands);

        // 3. Danh sách màu sắc từ các biến thể ACTIVE của sản phẩm ACTIVE
        List<String> colors = productVariantRepository.findActiveColors();

        // 4. Danh sách kích cỡ size sắp xếp tăng dần
        List<String> rawSizes = productVariantRepository.findActiveSizes();
        List<String> sortedSizes = rawSizes.stream()
                .sorted((s1, s2) -> {
                    try {
                        Integer n1 = Integer.parseInt(s1.trim());
                        Integer n2 = Integer.parseInt(s2.trim());
                        return n1.compareTo(n2);
                    } catch (NumberFormatException e) {
                        return s1.compareToIgnoreCase(s2);
                    }
                })
                .toList();

        // 5. Khoảng giá (min/max)
        BigDecimal minPrice = productVariantRepository.findMinActivePrice();
        BigDecimal maxPrice = productVariantRepository.findMaxActivePrice();

        return ShopFilterDataDTO.builder()
                .categories(categoryDtos)
                .brands(brandDtos)
                .colors(colors)
                .sizes(sortedSizes)
                .minPrice(minPrice != null ? minPrice : BigDecimal.ZERO)
                .maxPrice(maxPrice != null ? maxPrice : BigDecimal.ZERO)
                .build();
    }

    @Override
    public ShopProductDetailDTO getProductDetail(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm.");
        }

        // 1. Tìm sản phẩm theo slug kèm Category và Brand
        Product product = productRepository.findBySlugWithCategoryAndBrand(slug.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với đường dẫn: " + slug));

        // Ném lỗi 404 nếu sản phẩm không ở trạng thái ACTIVE (ví dụ đang HIDDEN)
        if (!ProductStatus.ACTIVE.equals(product.getStatus())) {
            throw new ResourceNotFoundException("Sản phẩm hiện không khả dụng hoặc đã ngừng kinh doanh.");
        }

        // 2. Lấy danh sách biến thể ACTIVE của sản phẩm
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndStatus(product.getId(), ProductVariantStatus.ACTIVE);

        // 3. Lấy tồn kho online cho toàn bộ biến thể cùng lúc (tránh N+1)
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
        Map<Long, Integer> stockMap = onlineAvailabilityService.getAvailableQuantities(variantIds);

        // 4. Lấy toàn bộ hình ảnh của sản phẩm
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId());
        List<ShopProductImageDTO> imageDtos = images.stream().map(img -> ShopProductImageDTO.builder()
                .id(img.getId())
                .variantId(img.getVariant() != null ? img.getVariant().getId() : null)
                .url(img.getUrl())
                .sortOrder(img.getSortOrder())
                .thumbnail(img.getIsThumbnail() != null && img.getIsThumbnail())
                .build()
        ).toList();

        // 5. Chuyển đổi danh sách biến thể sang DTO
        List<ShopProductVariantDTO> variantDtos = variants.stream().map(v -> {
            int stock = stockMap.getOrDefault(v.getId(), 0);

            // Tìm ảnh gắn riêng cho biến thể này nếu có
            String varImg = images.stream()
                    .filter(img -> img.getVariant() != null && img.getVariant().getId().equals(v.getId()))
                    .map(ProductImage::getUrl)
                    .findFirst()
                    .orElse(product.getThumbnailUrl());

            return ShopProductVariantDTO.builder()
                    .id(v.getId())
                    .sku(v.getSku())
                    .color(v.getColor())
                    .size(v.getSize())
                    .price(v.getPrice())
                    .availableStock(stock)
                    .imageUrl(varImg)
                    .build();
        }).toList();

        // Thu thập danh sách màu và size riêng biệt
        List<String> colors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> sizes = variants.stream()
                .map(ProductVariant::getSize)
                .filter(Objects::nonNull)
                .distinct()
                .sorted((s1, s2) -> {
                    try {
                        Integer n1 = Integer.parseInt(s1.trim());
                        Integer n2 = Integer.parseInt(s2.trim());
                        return n1.compareTo(n2);
                    } catch (NumberFormatException e) {
                        return s1.compareToIgnoreCase(s2);
                    }
                })
                .toList();

        BigDecimal minPrice = variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        boolean inStock = variants.stream()
                .anyMatch(v -> stockMap.getOrDefault(v.getId(), 0) > 0);

        return ShopProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl() != null && !product.getThumbnailUrl().isBlank()
                        ? product.getThumbnailUrl() : "/images/placeholder-shoe.svg")
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .categorySlug(product.getCategory() != null ? product.getCategory().getSlug() : "")
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : "")
                .images(imageDtos)
                .variants(variantDtos)
                .colors(colors)
                .sizes(sizes)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .inStock(inStock)
                .build();
    }

    @Override
    public List<ShopProductItemDTO> latestProducts(int limit) {
        int pageSize = (limit > 0) ? limit : 8;
        ShopProductFilterDTO filter = ShopProductFilterDTO.builder().sort("latest").build();
        Page<ShopProductItemDTO> page = listProducts(filter, PageRequest.of(0, pageSize));
        return page.getContent();
    }

    // =========================================================================
    // HÀM BỔ TRỢ: XÂY DỰNG SPECIFICATION VÀ ENRICH DỮ LIỆU CHỐNG N+1
    // =========================================================================

    private Specification<Product> buildProductSpecification(ShopProductFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Chỉ lấy sản phẩm có trạng thái ACTIVE
            predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

            // 2. Subquery biến thể: sản phẩm phải có ít nhất 1 biến thể ACTIVE thỏa mãn các điều kiện màu, size, giá, tồn
            Subquery<Long> varSub = query.subquery(Long.class);
            Root<ProductVariant> vRoot = varSub.from(ProductVariant.class);
            List<Predicate> vPreds = new ArrayList<>();
            vPreds.add(cb.equal(vRoot.get("product"), root));
            vPreds.add(cb.equal(vRoot.get("status"), ProductVariantStatus.ACTIVE));

            if (filter.getColor() != null && !filter.getColor().isBlank()) {
                vPreds.add(cb.equal(vRoot.get("color"), filter.getColor().trim()));
            }

            if (filter.getSize() != null && !filter.getSize().isBlank()) {
                vPreds.add(cb.equal(vRoot.get("size"), filter.getSize().trim()));
            }

            if (filter.getMinPrice() != null) {
                vPreds.add(cb.greaterThanOrEqualTo(vRoot.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                vPreds.add(cb.lessThanOrEqualTo(vRoot.get("price"), filter.getMaxPrice()));
            }

            if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                Store warehouse = onlineAvailabilityService.getSourceWarehouse();
                Subquery<Long> invSub = query.subquery(Long.class);
                Root<Inventory> invRoot = invSub.from(Inventory.class);
                invSub.select(cb.literal(1L));
                invSub.where(
                        cb.equal(invRoot.get("variant"), vRoot),
                        cb.equal(invRoot.get("store").get("id"), warehouse.getId()),
                        cb.greaterThan(invRoot.get("quantityOnHand"), 0)
                );
                vPreds.add(cb.exists(invSub));
            }

            varSub.select(cb.literal(1L));
            varSub.where(vPreds.toArray(new Predicate[0]));
            predicates.add(cb.exists(varSub));

            // 3. Lọc theo từ khóa (tên sản phẩm hoặc tên thương hiệu)
            Join<Product, Brand> brandJoin = null;
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                brandJoin = root.join("brand", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), kw),
                        cb.like(cb.lower(brandJoin.get("name")), kw)
                ));
            }

            // 4. Lọc theo danh mục (gốc hoặc danh mục con)
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                Join<Product, Category> catJoin = root.join("category", JoinType.INNER);
                Join<Category, Category> parentJoin = catJoin.join("parent", JoinType.LEFT);
                String catSlug = filter.getCategory().trim();
                predicates.add(cb.or(
                        cb.equal(catJoin.get("slug"), catSlug),
                        cb.equal(parentJoin.get("slug"), catSlug)
                ));
            }

            // 5. Lọc theo thương hiệu (nhiều thương hiệu)
            if (filter.getBrands() != null && !filter.getBrands().isEmpty()) {
                if (brandJoin == null) {
                    brandJoin = root.join("brand", JoinType.INNER);
                }
                predicates.add(brandJoin.get("slug").in(filter.getBrands()));
            }

            query.distinct(true);

            // 6. Xử lý sắp xếp (chỉ áp dụng khi không phải count query)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                String sort = (filter.getSort() != null) ? filter.getSort() : "latest";
                if ("price_asc".equalsIgnoreCase(sort)) {
                    Subquery<BigDecimal> minPriceSub = query.subquery(BigDecimal.class);
                    Root<ProductVariant> pv = minPriceSub.from(ProductVariant.class);
                    minPriceSub.select(cb.min(pv.get("price")));
                    minPriceSub.where(cb.equal(pv.get("product"), root), cb.equal(pv.get("status"), ProductVariantStatus.ACTIVE));
                    query.orderBy(cb.asc(minPriceSub), cb.desc(root.get("id")));
                } else if ("price_desc".equalsIgnoreCase(sort)) {
                    Subquery<BigDecimal> minPriceSub = query.subquery(BigDecimal.class);
                    Root<ProductVariant> pv = minPriceSub.from(ProductVariant.class);
                    minPriceSub.select(cb.min(pv.get("price")));
                    minPriceSub.where(cb.equal(pv.get("product"), root), cb.equal(pv.get("status"), ProductVariantStatus.ACTIVE));
                    query.orderBy(cb.desc(minPriceSub), cb.desc(root.get("id")));
                } else if ("name_asc".equalsIgnoreCase(sort)) {
                    query.orderBy(cb.asc(root.get("name")));
                } else {
                    query.orderBy(cb.desc(root.get("createdAt")), cb.desc(root.get("id")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<ShopProductItemDTO> enrichProducts(List<Product> products) {
        List<Long> productIds = products.stream().map(Product::getId).toList();

        // Nạp tất cả biến thể ACTIVE cho danh sách sản phẩm trang này (1 query)
        List<ProductVariant> variants = productVariantRepository.findByProductIdInAndStatus(productIds, ProductVariantStatus.ACTIVE);

        // Nạp tồn kho online cho toàn bộ biến thể (1 query)
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
        Map<Long, Integer> stockMap = onlineAvailabilityService.getAvailableQuantities(variantIds);

        // Gom nhóm biến thể theo productId
        Map<Long, List<ProductVariant>> variantsByProduct = variants.stream()
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        return products.stream().map(p -> {
            List<ProductVariant> pVars = variantsByProduct.getOrDefault(p.getId(), Collections.emptyList());

            BigDecimal minPrice = pVars.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            boolean inStock = pVars.stream()
                    .anyMatch(v -> stockMap.getOrDefault(v.getId(), 0) > 0);

            String thumb = (p.getThumbnailUrl() != null && !p.getThumbnailUrl().isBlank())
                    ? p.getThumbnailUrl()
                    : "/images/placeholder-shoe.svg";

            return ShopProductItemDTO.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .slug(p.getSlug())
                    .brandName(p.getBrand() != null ? p.getBrand().getName() : "")
                    .categoryName(p.getCategory() != null ? p.getCategory().getName() : "")
                    .categorySlug(p.getCategory() != null ? p.getCategory().getSlug() : "")
                    .thumbnailUrl(thumb)
                    .minPrice(minPrice)
                    .inStock(inStock)
                    .build();
        }).toList();
    }
}

