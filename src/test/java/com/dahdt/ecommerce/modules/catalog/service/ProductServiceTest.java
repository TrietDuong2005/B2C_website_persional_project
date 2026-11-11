package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.ProductRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductResponseDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductVariantResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Category;
import com.dahdt.ecommerce.modules.catalog.entity.Product;
import com.dahdt.ecommerce.modules.catalog.entity.ProductStatus;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariantStatus;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductImageMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductMapper;
import com.dahdt.ecommerce.modules.catalog.mapper.ProductVariantMapper;
import com.dahdt.ecommerce.modules.catalog.repository.BrandRepository;
import com.dahdt.ecommerce.modules.catalog.repository.CategoryRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductImageRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductRepository;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ ProductService & ProductVariantService")
class ProductServiceTest {

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
    private ProductMapper productMapper;

    @Mock
    private ProductVariantMapper productVariantMapper;

    @Mock
    private ProductImageMapper productImageMapper;

    @Mock
    private FileStorageService fileStorageService;

    private ProductServiceImpl productService;
    private ProductVariantServiceImpl productVariantService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(
                productRepository,
                productVariantRepository,
                productImageRepository,
                categoryRepository,
                brandRepository,
                productMapper,
                productVariantMapper,
                productImageMapper
        );

        productVariantService = new ProductVariantServiceImpl(
                productVariantRepository,
                productRepository,
                productImageRepository,
                productVariantMapper,
                productImageMapper,
                fileStorageService
        );

        testCategory = Category.builder()
                .id(1L)
                .name("Áo thun nam")
                .slug("ao-thun-nam")
                .build();

        testProduct = Product.builder()
                .id(10L)
                .category(testCategory)
                .name("Áo thun nam basic")
                .slug("ao-thun-nam-basic")
                .status(ProductStatus.ACTIVE)
                .variants(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Chặn tạo biến thể khi mã SKU đã tồn tại (trùng SKU)")
    void testCreateVariantDuplicateSkuThrowsBadRequestException() {
        ProductVariantRequestDTO request = ProductVariantRequestDTO.builder()
                .productId(10L)
                .sku("COOL-BLK-M")
                .color("Đen")
                .size("M")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("90000"))
                .weightGram(200)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.existsBySku("COOL-BLK-M")).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                productVariantService.createVariant(request)
        );

        assertTrue(exception.getMessage().contains("Mã SKU 'COOL-BLK-M' đã tồn tại"));
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn tạo biến thể khi giá bán <= 0")
    void testCreateVariantInvalidPriceThrowsBadRequestException() {
        ProductVariantRequestDTO requestZeroPrice = ProductVariantRequestDTO.builder()
                .productId(10L)
                .sku("COOL-BLK-M")
                .color("Đen")
                .size("M")
                .price(BigDecimal.ZERO)
                .costPrice(new BigDecimal("90000"))
                .weightGram(200)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        BadRequestException ex1 = assertThrows(BadRequestException.class, () ->
                productVariantService.createVariant(requestZeroPrice)
        );
        assertTrue(ex1.getMessage().contains("Giá bán của biến thể phải lớn hơn 0"));

        ProductVariantRequestDTO requestNegativePrice = ProductVariantRequestDTO.builder()
                .productId(10L)
                .sku("COOL-BLK-L")
                .color("Đen")
                .size("L")
                .price(new BigDecimal("-50000"))
                .costPrice(new BigDecimal("90000"))
                .weightGram(200)
                .build();

        BadRequestException ex2 = assertThrows(BadRequestException.class, () ->
                productVariantService.createVariant(requestNegativePrice)
        );
        assertTrue(ex2.getMessage().contains("Giá bán của biến thể phải lớn hơn 0"));
    }

    @Test
    @DisplayName("Chặn tạo biến thể khi giá vốn < 0")
    void testCreateVariantNegativeCostPriceThrowsBadRequestException() {
        ProductVariantRequestDTO requestNegativeCost = ProductVariantRequestDTO.builder()
                .productId(10L)
                .sku("COOL-BLK-XL")
                .color("Đen")
                .size("XL")
                .price(new BigDecimal("199000"))
                .costPrice(new BigDecimal("-1000"))
                .weightGram(200)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                productVariantService.createVariant(requestNegativeCost)
        );
        assertTrue(ex.getMessage().contains("Giá vốn của biến thể phải từ 0 trở lên"));
    }

    @Test
    @DisplayName("Tạo biến thể thành công khi thông tin hợp lệ")
    void testCreateVariantSuccess() {
        ProductVariantRequestDTO request = ProductVariantRequestDTO.builder()
                .productId(10L)
                .sku("COOL-WHT-S")
                .color("Trắng")
                .size("S")
                .price(new BigDecimal("189000"))
                .costPrice(new BigDecimal("80000"))
                .weightGram(180)
                .status(ProductVariantStatus.ACTIVE)
                .build();

        ProductVariant entity = ProductVariant.builder()
                .id(101L)
                .product(testProduct)
                .sku("COOL-WHT-S")
                .color("Trắng")
                .size("S")
                .price(new BigDecimal("189000"))
                .costPrice(new BigDecimal("80000"))
                .weightGram(180)
                .status(ProductVariantStatus.ACTIVE)
                .build();

        ProductVariantResponseDTO responseDTO = ProductVariantResponseDTO.builder()
                .id(101L)
                .sku("COOL-WHT-S")
                .color("Trắng")
                .size("S")
                .price(new BigDecimal("189000"))
                .status(ProductVariantStatus.ACTIVE)
                .sellable(true)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(productVariantRepository.existsBySku("COOL-WHT-S")).thenReturn(false);
        when(productVariantMapper.toEntity(request)).thenReturn(entity);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(entity);
        when(productVariantMapper.toDto(entity)).thenReturn(responseDTO);

        ProductVariantResponseDTO result = productVariantService.createVariant(request);

        assertNotNull(result);
        assertEquals("COOL-WHT-S", result.getSku());
        assertEquals(0, new BigDecimal("189000").compareTo(result.getPrice()));
        verify(productVariantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    @DisplayName("Tạo sản phẩm tự động sinh slug duy nhất và gán danh mục")
    void testCreateProductAutoGeneratesUniqueSlug() {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .categoryId(1L)
                .name("Áo Thun Nam Cổ Tròn Mới")
                .description("Vải cotton mát mẻ")
                .status(ProductStatus.ACTIVE)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySlug(anyString())).thenReturn(false);

        Product productEntity = Product.builder()
                .id(11L)
                .name("Áo Thun Nam Cổ Tròn Mới")
                .category(testCategory)
                .status(ProductStatus.ACTIVE)
                .build();

        ProductResponseDTO expectedDto = ProductResponseDTO.builder()
                .id(11L)
                .name("Áo Thun Nam Cổ Tròn Mới")
                .slug("ao-thun-nam-co-tron-moi")
                .categoryId(1L)
                .categoryName("Áo thun nam")
                .status(ProductStatus.ACTIVE)
                .build();

        when(productMapper.toEntity(request)).thenReturn(productEntity);
        when(productRepository.save(any(Product.class))).thenReturn(productEntity);
        when(productMapper.toDto(productEntity)).thenReturn(expectedDto);

        ProductResponseDTO result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("ao-thun-nam-co-tron-moi", result.getSlug());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}

