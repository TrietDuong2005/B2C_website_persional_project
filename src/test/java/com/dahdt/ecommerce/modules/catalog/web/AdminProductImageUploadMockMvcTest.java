package com.dahdt.ecommerce.modules.catalog.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.ProductImageResponseDTO;
import com.dahdt.ecommerce.modules.catalog.service.BrandService;
import com.dahdt.ecommerce.modules.catalog.service.CategoryService;
import com.dahdt.ecommerce.modules.catalog.service.ProductService;
import com.dahdt.ecommerce.modules.catalog.service.ProductVariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử MockMvc độc lập (standaloneSetup) cho endpoint thêm ảnh sản phẩm qua multipart")
class AdminProductImageUploadMockMvcTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private ProductVariantService productVariantService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private AdminProductPageController adminProductPageController;

    private static final byte[] SAMPLE_IMAGE_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminProductPageController).build();
    }

    @Test
    @DisplayName("Tải lên tệp ảnh hợp lệ thành công: Chuyển hướng về /admin/products/{id} kèm thông báo flash thành công")
    void testUploadImageFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "shoe.jpg", "image/jpeg", SAMPLE_IMAGE_BYTES
        );

        when(productVariantService.addProductImage(any(ProductImageRequestDTO.class)))
                .thenReturn(ProductImageResponseDTO.builder().id(100L).url("/uploads/products/sample.jpg").build());

        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L)
                        .file(file)
                        .param("sortOrder", "1")
                        .param("isThumbnail", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attribute("successMessage", "Thêm ảnh thành công!"));

        verify(productVariantService, times(1)).addProductImage(any(ProductImageRequestDTO.class));
    }

    @Test
    @DisplayName("Thêm ảnh bằng URL hợp lệ thành công: Chuyển hướng kèm flash message thành công")
    void testAddImageByUrlSuccess() throws Exception {
        when(productVariantService.addProductImage(any(ProductImageRequestDTO.class)))
                .thenReturn(ProductImageResponseDTO.builder().id(101L).url("https://example.com/shoe.jpg").build());

        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L)
                        .param("url", "https://example.com/shoe.jpg")
                        .param("sortOrder", "0")
                        .param("isThumbnail", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attribute("successMessage", "Thêm ảnh thành công!"));

        verify(productVariantService, times(1)).addProductImage(any(ProductImageRequestDTO.class));
    }

    @Test
    @DisplayName("Gửi cả tệp tải lên VÀ URL cùng lúc: Bị chặn với thông báo flash thân thiện")
    void testUploadBothFileAndUrlFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "shoe.jpg", "image/jpeg", SAMPLE_IMAGE_BYTES
        );

        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L)
                        .file(file)
                        .param("url", "https://example.com/shoe.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage",
                        "Chỉ được chọn một trong hai phương thức: tải tệp lên hoặc nhập URL hình ảnh, không dùng cả hai!"));

        verify(productVariantService, never()).addProductImage(any(ProductImageRequestDTO.class));
    }

    @Test
    @DisplayName("Không chọn tệp và cũng không nhập URL: Bị chặn với thông báo flash yêu cầu chọn")
    void testUploadNeitherFileNorUrlFails() throws Exception {
        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage",
                        "Vui lòng chọn tệp ảnh tải lên hoặc nhập URL hình ảnh!"));

        verify(productVariantService, never()).addProductImage(any(ProductImageRequestDTO.class));
    }

    @Test
    @DisplayName("Service ném BadRequestException (ví dụ sai định dạng/magic bytes): Chuyển hướng kèm flash errorMessage")
    void testUploadServiceBadRequestException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "fake.jpg", "image/jpeg", new byte[]{'M', 'Z'}
        );

        when(productVariantService.addProductImage(any(ProductImageRequestDTO.class)))
                .thenThrow(new BadRequestException("Chữ ký tệp (magic bytes) không khớp với ảnh thật"));

        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L)
                        .file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attribute("errorMessage", "Chữ ký tệp (magic bytes) không khớp với ảnh thật"));

        verify(productVariantService, times(1)).addProductImage(any(ProductImageRequestDTO.class));
    }

    @Test
    @DisplayName("Xử lý ngoại lệ MaxUploadSizeExceededException: Chuyển hướng về trang sản phẩm với flash thông báo thân thiện")
    void testHandleMaxUploadSizeExceededDirectly() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(2 * 1024 * 1024);
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
        request.setRequestURI("/admin/products/7/images/save");
        org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap redirectAttributes =
                new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap();

        String view = adminProductPageController.handleMaxUploadSizeExceeded(ex, request, redirectAttributes);

        assertEquals("redirect:/admin/products/7", view, "Phải chuyển hướng đúng về trang chi tiết sản phẩm: " + view);
        assertNotNull(redirectAttributes.getFlashAttributes().get("errorMessage"),
                "Flash attribute errorMessage phải tồn tại");
        assertEquals("Dung lượng tệp tải lên vượt quá giới hạn tối đa cho phép!",
                redirectAttributes.getFlashAttributes().get("errorMessage"),
                "Thông điệp báo lỗi kích thước phải thân thiện tiếng Việt");
    }

    @Test
    @DisplayName("Thêm ảnh theo màu qua multipart (target='color:Hồng Phấn'): Gọi service với color và variantId = null")
    void testUploadImageFileByColorTargetSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "pink-shoe.jpg", "image/jpeg", SAMPLE_IMAGE_BYTES
        );

        when(productVariantService.addProductImage(any(ProductImageRequestDTO.class)))
                .thenReturn(ProductImageResponseDTO.builder().id(105L).url("/uploads/products/pink-shoe.jpg").build());

        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L)
                        .file(file)
                        .param("target", "color:Hồng Phấn")
                        .param("sortOrder", "1")
                        .param("isThumbnail", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attribute("successMessage", "Thêm ảnh thành công!"));

        ArgumentCaptor<ProductImageRequestDTO> captor = ArgumentCaptor.forClass(ProductImageRequestDTO.class);
        verify(productVariantService, times(1)).addProductImage(captor.capture());

        ProductImageRequestDTO captured = captor.getValue();
        assertEquals("Hồng Phấn", captured.getColor(),
                "Color gửi sang service phải là 'Hồng Phấn'. Thực tế: " + captured.getColor());
        assertNull(captured.getVariantId(),
                "Khi gắn theo màu, variantId phải là null. Thực tế: " + captured.getVariantId());
    }

    @Test
    @DisplayName("Thêm ảnh gắn biến thể cụ thể qua multipart (target='variant:101'): Gọi service với variantId = 101 và color = null")
    void testUploadImageFileByVariantTargetSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "variant-shoe.jpg", "image/jpeg", SAMPLE_IMAGE_BYTES
        );

        when(productVariantService.addProductImage(any(ProductImageRequestDTO.class)))
                .thenReturn(ProductImageResponseDTO.builder().id(106L).url("/uploads/products/var.jpg").build());

        mockMvc.perform(multipart("/admin/products/{id}/images/save", 5L)
                        .file(file)
                        .param("target", "variant:101")
                        .param("sortOrder", "2")
                        .param("isThumbnail", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products/5"))
                .andExpect(flash().attribute("successMessage", "Thêm ảnh thành công!"));

        ArgumentCaptor<ProductImageRequestDTO> captor = ArgumentCaptor.forClass(ProductImageRequestDTO.class);
        verify(productVariantService, times(1)).addProductImage(captor.capture());

        ProductImageRequestDTO captured = captor.getValue();
        assertEquals(101L, captured.getVariantId(),
                "VariantId gửi sang service phải là 101. Thực tế: " + captured.getVariantId());
        assertNull(captured.getColor(),
                "Khi gắn theo biến thể, color phải là null. Thực tế: " + captured.getColor());
    }
}

