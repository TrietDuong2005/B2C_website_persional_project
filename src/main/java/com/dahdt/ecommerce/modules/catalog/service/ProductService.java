package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.entity.*;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductMapper;
import com.dahdt.ecommerce.modules.catalog.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;

    /**
     * API phân trang và lọc sản phẩm cho user
     */
    public Page<ProductResponseDTO> searchProducts(Long categoryId, Long brandId, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), false));
            predicates.add(cb.equal(root.get("status"), "active"));

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::mapToFullProductDto);
    }

    /**
     * Xem chi tiết 1 sản phẩm
     */
    public ProductResponseDTO getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Sản phẩm đã bị xóa");
        }
        return mapToFullProductDto(product);
    }

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        Product product = productMapper.toEntity(request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand không tồn tại"));

        product.setCategory(category);
        product.setBrand(brand);

        product = productRepository.save(product);
        return mapToFullProductDto(product);
    }

    @Transactional
    public ProductResponseDTO addVariant(ProductVariantRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        ProductVariant variant = productMapper.toVariantEntity(request);
        variant.setProduct(product);

        // Gắn danh sách thuộc tính (ví dụ Đỏ, Size M)
        if (request.getAttributeValueIds() != null && !request.getAttributeValueIds().isEmpty()) {
            Set<AttributeValue> attributeValues = request.getAttributeValueIds().stream()
                    .map(id -> attributeValueRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Attribute Value ID " + id + " không hợp lệ")))
                    .collect(Collectors.toSet());
            variant.setAttributeValues(attributeValues);
        }

        productVariantRepository.save(variant);
        return mapToFullProductDto(product);
    }

    @Transactional
    public void addImage(ProductImageRequestDTO request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant không tồn tại"));
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setVariant(variant);
        image.setUrl(request.getUrl());
        image.setSortOrder(request.getSortOrder());
        image.setThumbnail(request.isThumbnail());

        productImageRepository.save(image);
    }

    /**
     * Hàm helper để map chi tiết từ Entity -> DTO kèm Variant, Image, Attribute
     */
    private ProductResponseDTO mapToFullProductDto(Product product) {
        ProductResponseDTO dto = productMapper.toDto(product);

        // 1. Map Variants
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsDeletedFalse(product.getId());
        List<ProductResponseDTO.ProductVariantResponseDTO> variantDtos = variants.stream().map(v -> {
            ProductResponseDTO.ProductVariantResponseDTO vDto = productMapper.toVariantDto(v);
            // Map AttributeValues của variant này
            Set<ProductResponseDTO.AttributeValueDTO> attrDtos = v.getAttributeValues().stream()
                    .map(productMapper::toAttributeValueDto)
                    .collect(Collectors.toSet());
            vDto.setAttributeValues(attrDtos);
            return vDto;
        }).collect(Collectors.toList());
        dto.setVariants(variantDtos);

        // 2. Map Images
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        List<ProductResponseDTO.ProductImageResponseDTO> imageDtos = images.stream()
                .map(productMapper::toImageDto)
                .collect(Collectors.toList());
        dto.setImages(imageDtos);

        return dto;
    }
}
