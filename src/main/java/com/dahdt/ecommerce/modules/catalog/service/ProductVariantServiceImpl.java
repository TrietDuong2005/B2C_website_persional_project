package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.*;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductImageMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductVariantMapper;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ quản lý Biến thể sản phẩm (ProductVariant) và Hình ảnh (ProductImage).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantMapper productVariantMapper;
    private final ProductImageMapper productImageMapper;
    private final FileStorageService fileStorageService;

    @Override
    public List<ProductVariantResponseDTO> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductIdOrderByIdAsc(productId).stream()
                .map(this::enrichVariantDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductVariantResponseDTO getVariantById(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể với ID: " + id));
        return enrichVariantDto(variant);
    }

    @Override
    @Transactional
    public ProductVariantResponseDTO createVariant(ProductVariantRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm cha ID: " + dto.getProductId()));

        // 1. Kiểm tra giá bán và giá vốn
        validatePrices(dto.getPrice(), dto.getCostPrice(), dto.getWeightGram());

        // 2. Kiểm tra tính duy nhất của mã SKU
        String cleanSku = dto.getSku().trim();
        if (productVariantRepository.existsBySku(cleanSku)) {
            throw new BadRequestException("Mã SKU '" + cleanSku + "' đã tồn tại trong hệ thống");
        }

        // 3. Kiểm tra tính duy nhất của barcode nếu có
        String cleanBarcode = (dto.getBarcode() != null && !dto.getBarcode().isBlank()) ? dto.getBarcode().trim() : null;
        if (cleanBarcode != null && productVariantRepository.existsByBarcode(cleanBarcode)) {
            throw new BadRequestException("Mã vạch barcode '" + cleanBarcode + "' đã tồn tại trong hệ thống");
        }

        ProductVariant variant = productVariantMapper.toEntity(dto);
        variant.setProduct(product);
        variant.setSku(cleanSku);
        variant.setBarcode(cleanBarcode);
        variant.setColor(dto.getColor().trim());
        variant.setSize(dto.getSize().trim());
        variant.setStatus(dto.getStatus() != null ? dto.getStatus() : ProductVariantStatus.ACTIVE);

        ProductVariant saved = productVariantRepository.save(variant);

        // Nếu người dùng nhập kèm URL ảnh khi tạo biến thể, lưu vào bảng product_images
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .variant(saved)
                    .url(dto.getImageUrl().trim())
                    .sortOrder(0)
                    .isThumbnail(false)
                    .build();
            productImageRepository.save(image);
        }

        return enrichVariantDto(saved);
    }

    @Override
    @Transactional
    public ProductVariantResponseDTO updateVariant(Long id, ProductVariantRequestDTO dto) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể với ID: " + id));

        // 1. Kiểm tra giá bán và giá vốn
        validatePrices(dto.getPrice(), dto.getCostPrice(), dto.getWeightGram());

        // 2. Kiểm tra SKU duy nhất
        String cleanSku = dto.getSku().trim();
        if (productVariantRepository.existsBySkuAndIdNot(cleanSku, id)) {
            throw new BadRequestException("Mã SKU '" + cleanSku + "' đã được sử dụng bởi biến thể khác");
        }

        // 3. Kiểm tra Barcode duy nhất nếu có
        String cleanBarcode = (dto.getBarcode() != null && !dto.getBarcode().isBlank()) ? dto.getBarcode().trim() : null;
        if (cleanBarcode != null && productVariantRepository.existsByBarcodeAndIdNot(cleanBarcode, id)) {
            throw new BadRequestException("Mã vạch barcode '" + cleanBarcode + "' đã được sử dụng bởi biến thể khác");
        }

        variant.setSku(cleanSku);
        variant.setBarcode(cleanBarcode);
        variant.setColor(dto.getColor().trim());
        variant.setSize(dto.getSize().trim());
        variant.setPrice(dto.getPrice());
        variant.setCostPrice(dto.getCostPrice() != null ? dto.getCostPrice() : BigDecimal.ZERO);
        variant.setWeightGram(dto.getWeightGram() != null ? dto.getWeightGram() : 0);
        if (dto.getStatus() != null) {
            variant.setStatus(dto.getStatus());
        }

        ProductVariant updated = productVariantRepository.save(variant);
        return enrichVariantDto(updated);
    }

    @Override
    @Transactional
    public void toggleVariantStatus(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể với ID: " + id));
        variant.setStatus(ProductVariantStatus.ACTIVE.equals(variant.getStatus())
                ? ProductVariantStatus.INACTIVE
                : ProductVariantStatus.ACTIVE);
        productVariantRepository.save(variant);
    }

    @Override
    @Transactional
    public void deleteVariant(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể với ID: " + id));
        // Quy tắc: Xoá biến thể = chuyển trạng thái sang INACTIVE, không xoá cứng
        variant.setStatus(ProductVariantStatus.INACTIVE);
        productVariantRepository.save(variant);
    }

    @Override
    @Transactional
    public ProductImageResponseDTO addProductImage(ProductImageRequestDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm cha ID: " + dto.getProductId()));

        ProductVariant variant = null;
        if (dto.getVariantId() != null) {
            variant = productVariantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể ID: " + dto.getVariantId()));
        }

        boolean hasUrl = dto.getUrl() != null && !dto.getUrl().trim().isEmpty();
        boolean hasFile = dto.getImageFile() != null && !dto.getImageFile().isEmpty();

        if (hasFile && hasUrl) {
            throw new BadRequestException("Chỉ được chọn một trong hai phương thức: tải tệp lên hoặc nhập URL hình ảnh, không dùng cả hai");
        }

        String finalUrl;
        if (hasFile) {
            finalUrl = fileStorageService.storeProductImage(dto.getImageFile());
        } else if (hasUrl) {
            finalUrl = dto.getUrl().trim();
        } else {
            throw new BadRequestException("Vui lòng chọn tệp ảnh tải lên hoặc nhập URL hình ảnh");
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .variant(variant)
                .url(finalUrl)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isThumbnail(Boolean.TRUE.equals(dto.getIsThumbnail()))
                .build();

        ProductImage saved = productImageRepository.save(image);

        // Nếu được đặt làm ảnh đại diện, cập nhật thumbnail của sản phẩm để trang danh sách và chi tiết hiển thị đúng
        if (Boolean.TRUE.equals(dto.getIsThumbnail())) {
            product.setThumbnailUrl(finalUrl);
            productRepository.save(product);
        }

        return productImageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hình ảnh ID: " + imageId));

        String imageUrl = image.getUrl();
        Product product = image.getProduct();

        productImageRepository.delete(image);

        // Xóa tệp vật lý nếu nằm trong thư mục uploads
        fileStorageService.deleteFile(imageUrl);

        // Nếu ảnh vừa xóa chính là ảnh đại diện của sản phẩm, cập nhật lại thumbnail
        if (product != null && imageUrl.equals(product.getThumbnailUrl())) {
            List<ProductImage> remaining = productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId());
            String newThumb = remaining.stream()
                    .filter(ProductImage::getIsThumbnail)
                    .map(ProductImage::getUrl)
                    .findFirst()
                    .orElse(!remaining.isEmpty() ? remaining.get(0).getUrl() : null);
            product.setThumbnailUrl(newThumb);
            productRepository.save(product);
        }
    }

    // =========================================================================
    // HÀM BỔ TRỢ
    // =========================================================================

    private void validatePrices(BigDecimal price, BigDecimal costPrice, Integer weightGram) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Giá bán của biến thể phải lớn hơn 0");
        }
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Giá vốn của biến thể phải từ 0 trở lên");
        }
        if (weightGram == null || weightGram < 0) {
            throw new BadRequestException("Khối lượng của biến thể phải từ 0 gram trở lên");
        }
    }

    private ProductVariantResponseDTO enrichVariantDto(ProductVariant variant) {
        ProductVariantResponseDTO dto = productVariantMapper.toDto(variant);
        List<ProductImage> varImages = productImageRepository.findByVariantIdOrderBySortOrderAscIdAsc(variant.getId());
        if (!varImages.isEmpty()) {
            dto.setImageUrl(varImages.get(0).getUrl());
        }
        return dto;
    }
}

