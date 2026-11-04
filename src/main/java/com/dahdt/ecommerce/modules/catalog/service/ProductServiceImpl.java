package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.common.util.SlugUtil;
import com.dahdt.ecommerce.modules.catalog.dto.*;
import com.dahdt.ecommerce.modules.catalog.entity.*;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductImageMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductVariantMapper;
import com.dahdt.ecommerce.modules.catalog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ quản lý Sản phẩm cha (Product).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductImageMapper productImageMapper;

    @Override
    public Page<ProductResponseDTO> getProducts(String keyword, Long categoryId, Long brandId, ProductStatus status, Pageable pageable) {
        String query = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Product> productPage = productRepository.searchProducts(query, categoryId, brandId, status, pageable);

        return productPage.map(this::enrichProductDto);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findByIdWithCategoryAndBrand(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return enrichProductDto(product);
    }

    @Override
    public ProductDetailResponseDTO getProductDetail(Long id) {
        Product product = productRepository.findByIdWithCategoryAndBrand(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        ProductDetailResponseDTO detailDTO = productMapper.toDetailDto(product);

        // Lấy danh sách biến thể
        List<ProductVariant> variants = productVariantRepository.findByProductIdOrderByIdAsc(id);
        List<ProductVariantResponseDTO> variantDtos = variants.stream()
                .map(v -> {
                    ProductVariantResponseDTO dto = productVariantMapper.toDto(v);
                    // Lấy ảnh riêng của biến thể nếu có
                    List<ProductImage> varImages = productImageRepository.findByVariantIdOrderBySortOrderAscIdAsc(v.getId());
                    if (!varImages.isEmpty()) {
                        dto.setImageUrl(varImages.get(0).getUrl());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        detailDTO.setVariants(variantDtos);

        // Lấy danh sách hình ảnh sản phẩm
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(id);
        detailDTO.setImages(productImageMapper.toDtoList(images));

        return detailDTO;
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục ID: " + dto.getCategoryId()));

        Brand brand = null;
        if (dto.getBrandId() != null) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu ID: " + dto.getBrandId()));
        }

        Product product = productMapper.toEntity(dto);
        product.setCategory(category);
        product.setBrand(brand);
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : ProductStatus.ACTIVE);

        // Tự sinh slug duy nhất từ tên sản phẩm
        product.setSlug(generateUniqueSlug(dto.getName(), null));

        Product saved = productRepository.save(product);
        return enrichProductDto(saved);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục ID: " + dto.getCategoryId()));

        Brand brand = null;
        if (dto.getBrandId() != null) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu ID: " + dto.getBrandId()));
        }

        product.setName(dto.getName().trim());
        product.setDescription(dto.getDescription());
        product.setThumbnailUrl(dto.getThumbnailUrl());
        product.setCategory(category);
        product.setBrand(brand);
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        // Tự sinh lại slug duy nhất
        product.setSlug(generateUniqueSlug(dto.getName(), id));

        Product updated = productRepository.save(product);
        return enrichProductDto(updated);
    }

    @Override
    @Transactional
    public void toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        product.setStatus(ProductStatus.ACTIVE.equals(product.getStatus()) ? ProductStatus.HIDDEN : ProductStatus.ACTIVE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        // Quy tắc: Xoá sản phẩm = chuyển trạng thái sang HIDDEN, không xoá cứng
        product.setStatus(ProductStatus.HIDDEN);
        productRepository.save(product);
    }

    // =========================================================================
    // HÀM BỔ TRỢ
    // =========================================================================

    private ProductResponseDTO enrichProductDto(Product product) {
        ProductResponseDTO dto = productMapper.toDto(product);

        List<ProductVariant> variants = productVariantRepository.findByProductIdOrderByIdAsc(product.getId());
        dto.setVariantCount(variants.size());

        if (!variants.isEmpty()) {
            BigDecimal min = variants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal max = variants.stream()
                    .map(ProductVariant::getPrice)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            dto.setMinPrice(min);
            dto.setMaxPrice(max);
        } else {
            dto.setMinPrice(BigDecimal.ZERO);
            dto.setMaxPrice(BigDecimal.ZERO);
        }

        return dto;
    }

    private String generateUniqueSlug(String name, Long existingId) {
        String baseSlug = SlugUtil.toSlug(name);
        if (baseSlug.isBlank()) {
            baseSlug = "san-pham";
        }
        String candidate = baseSlug;
        int count = 1;
        while (existingId == null
                ? productRepository.existsBySlug(candidate)
                : productRepository.existsBySlugAndIdNot(candidate, existingId)) {
            candidate = baseSlug + "-" + count++;
        }
        return candidate;
    }
}

