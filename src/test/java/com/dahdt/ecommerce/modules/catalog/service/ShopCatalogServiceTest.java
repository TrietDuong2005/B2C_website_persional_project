package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.shop.ShopProductDetailDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Brand;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.mapper.BrandMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.CategoryMapper;
import com.dahdt.ecommerce.modules.catalog.repository.BrandRepository;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.fulfillment.OnlineAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ ShopCatalogService (Xem và tìm kiếm sản phẩm Shop)")
class ShopCatalogServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private BrandMapper brandMapper;

    @Mock
    private OnlineAvailabilityService onlineAvailabilityService;

    @InjectMocks
    private ShopCatalogServiceImpl shopCatalogService;

    private Product activeProduct;
    private Product hiddenProduct;
    private ProductVariant activeVariant;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Sneaker").slug("sneaker").build();
        Brand brand = Brand.builder().id(2L).name("Nike").slug("nike").build();

        activeProduct = Product.builder()
                .id(10L)
                .name("Nike Air Force 1")
                .slug("nike-air-force-1")
                .category(category)
                .brand(brand)
                .status(ProductStatus.ACTIVE)
                .build();

        hiddenProduct = Product.builder()
                .id(20L)
                .name("Giày Mẫu Cũ")
                .slug("giay-mau-cu")
                .category(category)
                .brand(brand)
                .status(ProductStatus.HIDDEN) // Đang ẩn
                .build();

        activeVariant = ProductVariant.builder()
                .id(101L)
                .product(activeProduct)
                .sku("NK-AF1-01")
                .color("Trắng")
                .size("42")
                .price(new BigDecimal("2600000"))
                .status(ProductVariantStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("getProductDetail(): Sản phẩm không tồn tại thì ném ResourceNotFoundException (lỗi 404)")
    void getProductDetail_whenNotFound_shouldThrowResourceNotFoundException() {
        when(productRepository.findBySlugWithCategoryAndBrand("san-pham-khong-ton-tai"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> shopCatalogService.getProductDetail("san-pham-khong-ton-tai"));

        assertTrue(ex.getMessage().contains("Không tìm thấy sản phẩm"));
        verify(productVariantRepository, never()).findByProductIdAndStatus(anyLong(), any());
    }

    @Test
    @DisplayName("getProductDetail(): Sản phẩm ở trạng thái HIDDEN bị chặn ném ResourceNotFoundException (lỗi 404)")
    void getProductDetail_whenProductIsHidden_shouldThrowResourceNotFoundException() {
        when(productRepository.findBySlugWithCategoryAndBrand("giay-mau-cu"))
                .thenReturn(Optional.of(hiddenProduct));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> shopCatalogService.getProductDetail("giay-mau-cu"));

        assertTrue(ex.getMessage().contains("không khả dụng hoặc đã ngừng kinh doanh"));
        verify(productVariantRepository, never()).findByProductIdAndStatus(anyLong(), any());
    }

    @Test
    @DisplayName("getProductDetail(): Sản phẩm ACTIVE trả đầy đủ thông tin chi tiết, biến thể và tồn online")
    void getProductDetail_whenProductIsActive_shouldReturnDetails() {
        when(productRepository.findBySlugWithCategoryAndBrand("nike-air-force-1"))
                .thenReturn(Optional.of(activeProduct));
        when(productVariantRepository.findByProductIdAndStatus(10L, ProductVariantStatus.ACTIVE))
                .thenReturn(List.of(activeVariant));
        when(productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(10L))
                .thenReturn(Collections.emptyList());
        when(onlineAvailabilityService.getAvailableQuantities(List.of(101L)))
                .thenReturn(Map.of(101L, 10));

        ShopProductDetailDTO detail = shopCatalogService.getProductDetail("nike-air-force-1");

        assertNotNull(detail);
        assertEquals("Nike Air Force 1", detail.getName());
        assertEquals("nike-air-force-1", detail.getSlug());
        assertEquals("Nike", detail.getBrandName());
        assertEquals(1, detail.getVariants().size());
        assertEquals(10, detail.getVariants().get(0).getAvailableStock());
        assertTrue(detail.isInStock());
        assertEquals(0, new BigDecimal("2600000").compareTo(detail.getMinPrice()));
    }
}

