package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductImage;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service / Bộ tiện ích phân giải hình ảnh cho biến thể sản phẩm (ProductImageResolver).
 * Áp dụng thứ tự ưu tiên 4 bước theo đúng quy định NGÀY 12C:
 *   (1) Ảnh gắn đúng biến thể cụ thể (variant_id trùng khớp)
 *   (2) Ảnh gắn màu của biến thể (color, so sánh không phân biệt hoa thường và khoảng trắng thừa)
 *   (3) Ảnh chung của sản phẩm (cả variant_id và color đều NULL) hoặc thumbnail sản phẩm
 *   (4) Ảnh minh họa mặc định (/images/placeholder-shoe.svg)
 */
@Component
@RequiredArgsConstructor
public class ProductImageResolver {

    /** Đường dẫn ảnh minh họa mặc định dùng chung toàn hệ thống */
    public static final String DEFAULT_PLACEHOLDER_IMAGE = "/images/placeholder-shoe.svg";

    private final ProductImageRepository productImageRepository;

    /**
     * Phân giải hình ảnh cho một biến thể bằng cách tự nạp toàn bộ ảnh của sản phẩm cha từ CSDL.
     *
     * @param variant biến thể sản phẩm cần tìm ảnh
     * @return đường dẫn URL của hình ảnh phù hợp nhất
     */
    public String resolveImageUrl(ProductVariant variant) {
        if (variant == null) {
            return DEFAULT_PLACEHOLDER_IMAGE;
        }

        Product product = variant.getProduct();
        String fallbackThumbnail = (product != null) ? product.getThumbnailUrl() : null;

        if (product == null || product.getId() == null) {
            return (fallbackThumbnail != null && !fallbackThumbnail.isBlank())
                    ? fallbackThumbnail : DEFAULT_PLACEHOLDER_IMAGE;
        }

        List<ProductImage> productImages = productImageRepository != null
                ? productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId())
                : Collections.emptyList();

        return resolveImageUrl(variant, productImages, fallbackThumbnail);
    }

    /**
     * Phương thức phân giải hình ảnh dùng danh sách ảnh đã nạp sẵn trong bộ nhớ (tránh N+1 query).
     *
     * @param variant biến thể sản phẩm cần hiển thị ảnh
     * @param productImages danh sách toàn bộ ảnh của sản phẩm cha (đã nạp sẵn)
     * @param fallbackThumbnail ảnh thumbnail mặc định của sản phẩm cha (nếu có)
     * @return đường dẫn URL của hình ảnh theo đúng thứ tự 4 bước
     */
    public static String resolveImageUrl(ProductVariant variant, List<ProductImage> productImages, String fallbackThumbnail) {
        if (variant == null) {
            return (fallbackThumbnail != null && !fallbackThumbnail.isBlank())
                    ? fallbackThumbnail : DEFAULT_PLACEHOLDER_IMAGE;
        }

        if (productImages == null || productImages.isEmpty()) {
            if (fallbackThumbnail != null && !fallbackThumbnail.isBlank()) {
                return fallbackThumbnail;
            }
            return DEFAULT_PLACEHOLDER_IMAGE;
        }

        // (1) Ảnh gắn đúng biến thể cụ thể (variant_id trùng khớp)
        if (variant.getId() != null) {
            Optional<ProductImage> variantImage = productImages.stream()
                    .filter(img -> img.getVariant() != null && variant.getId().equals(img.getVariant().getId()))
                    .findFirst();
            if (variantImage.isPresent() && isValidUrl(variantImage.get().getUrl())) {
                return variantImage.get().getUrl();
            }
        }

        // (2) Ảnh gắn màu của biến thể (so sánh không phân biệt hoa thường và khoảng trắng thừa)
        if (variant.getColor() != null && !variant.getColor().isBlank()) {
            String variantColor = variant.getColor().trim();
            Optional<ProductImage> colorImage = productImages.stream()
                    .filter(img -> img.getVariant() == null && matchesColor(img.getColor(), variantColor))
                    .findFirst();
            if (colorImage.isPresent() && isValidUrl(colorImage.get().getUrl())) {
                return colorImage.get().getUrl();
            }
        }

        // (3) Ảnh chung của sản phẩm (cả variant_id và color đều NULL hoặc rỗng)
        // Ưu tiên ảnh chung được đánh dấu isThumbnail nếu có, sau đó đến ảnh chung đầu tiên theo sort_order
        Optional<ProductImage> generalThumbnail = productImages.stream()
                .filter(img -> isGeneralImage(img) && Boolean.TRUE.equals(img.getIsThumbnail()))
                .findFirst();
        if (generalThumbnail.isPresent() && isValidUrl(generalThumbnail.get().getUrl())) {
            return generalThumbnail.get().getUrl();
        }

        Optional<ProductImage> generalImage = productImages.stream()
                .filter(ProductImageResolver::isGeneralImage)
                .findFirst();
        if (generalImage.isPresent() && isValidUrl(generalImage.get().getUrl())) {
            return generalImage.get().getUrl();
        }

        // Nếu không có ảnh chung trong gallery nhưng sản phẩm cha có thumbnailUrl
        if (fallbackThumbnail != null && !fallbackThumbnail.isBlank()) {
            return fallbackThumbnail;
        }

        // (4) Ảnh minh họa mặc định
        return DEFAULT_PLACEHOLDER_IMAGE;
    }

    /**
     * So sánh hai chuỗi màu sắc không phân biệt chữ hoa/thường và khoảng trắng thừa.
     *
     * @param color1 màu thứ nhất
     * @param color2 màu thứ hai
     * @return true nếu cùng một màu
     */
    public static boolean matchesColor(String color1, String color2) {
        if (color1 == null || color2 == null) {
            return false;
        }
        return color1.trim().equalsIgnoreCase(color2.trim());
    }

    /**
     * Kiểm tra một ảnh có phải là ảnh chung cho toàn sản phẩm hay không (variant == null và color == null/rỗng).
     *
     * @param img hình ảnh cần kiểm tra
     * @return true nếu là ảnh chung
     */
    public static boolean isGeneralImage(ProductImage img) {
        return img != null
                && img.getVariant() == null
                && (img.getColor() == null || img.getColor().isBlank());
    }

    private static boolean isValidUrl(String url) {
        return url != null && !url.isBlank();
    }
}

