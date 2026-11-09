package com.dahdt.ecommerce.modules.pos.controller;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.GlobalExceptionHandler;
import com.dahdt.ecommerce.common.exception.InsufficientStockException;
import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.pos.dto.*;
import com.dahdt.ecommerce.modules.pos.service.PosCatalogService;
import com.dahdt.ecommerce.modules.pos.service.PosSaleService;
import com.dahdt.ecommerce.modules.user.entity.Role;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Kiểm thử REST Controller cho POS (/pos/api/**)")
class PosApiControllerTest {

    private MockMvc mockMvc;
    private PosCatalogService posCatalogService;
    private PosSaleService posSaleService;
    private ObjectMapper objectMapper;

    private CustomUserDetails cashierUserDetails;
    private UsernamePasswordAuthenticationToken authPrincipal;

    @BeforeEach
    void setUp() {
        posCatalogService = mock(PosCatalogService.class);
        posSaleService = mock(PosSaleService.class);
        objectMapper = new ObjectMapper();

        User cashierUser = User.builder()
                .id(10L)
                .email("cashier@dahdt.com")
                .roles(Set.of(Role.builder().code("CASHIER").build()))
                .build();

        cashierUserDetails = new CustomUserDetails(cashierUser);
        authPrincipal = new UsernamePasswordAuthenticationToken(cashierUserDetails, null, cashierUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authPrincipal);

        // Argument resolver hỗ trợ phân giải @AuthenticationPrincipal CustomUserDetails trong MockMvc standaloneSetup
        HandlerMethodArgumentResolver authenticationPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                Principal userPrincipal = webRequest.getUserPrincipal();
                if (userPrincipal instanceof Authentication auth && auth.getPrincipal() instanceof CustomUserDetails) {
                    return auth.getPrincipal();
                }
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
                    return auth.getPrincipal();
                }
                return null;
            }
        };

        PosApiController controller = new PosApiController(posCatalogService, posSaleService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver(), authenticationPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /pos/api/products: Thành công trả danh sách sản phẩm kèm tồn kho (mã 200)")
    void testSearchProducts_Success() throws Exception {
        PosProductSearchResponseDTO item = PosProductSearchResponseDTO.builder()
                .variantId(1L)
                .sku("SNK-01-42")
                .barcode("893000000001")
                .productName("Giày Sneaker Triết")
                .color("Trắng")
                .size("42")
                .unitPrice(new BigDecimal("500000"))
                .quantityOnHand(15)
                .build();

        when(posCatalogService.search(eq(10L), eq("SNK"))).thenReturn(List.of(item));

        MvcResult result = mockMvc.perform(get("/pos/api/products")
                        .principal(authPrincipal)
                        .param("q", "SNK"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].sku").value("SNK-01-42"))
                .andExpect(jsonPath("$.data[0].quantityOnHand").value(15))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(200, result.getResponse().getStatus(),
                "Mã HTTP phải là 200. Thực tế nhận được: " + result.getResponse().getStatus() + ", Nội dung phản hồi: " + responseBody);
        assertTrue(responseBody.contains("SNK-01-42"),
                "Nội dung phản hồi phải chứa SKU 'SNK-01-42'. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("GET /pos/api/products: Tài khoản không thuộc cửa hàng (Admin) trả mã 400 thông báo tiếng Việt")
    void testSearchProducts_NoStore_ReturnsBadRequest() throws Exception {
        when(posCatalogService.search(eq(10L), any()))
                .thenThrow(new BadRequestException("Tài khoản không thuộc cửa hàng nào."));

        MvcResult result = mockMvc.perform(get("/pos/api/products")
                        .principal(authPrincipal)
                        .param("q", "SNK"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Tài khoản không thuộc cửa hàng nào."))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(400, result.getResponse().getStatus(),
                "Mã HTTP phải là 400 khi tài khoản không thuộc cửa hàng. Thực tế: " + result.getResponse().getStatus() + ", Nội dung phản hồi: " + responseBody);
        assertTrue(responseBody.contains("Tài khoản không thuộc cửa hàng nào."),
                "Thông điệp phản hồi phải chứa 'Tài khoản không thuộc cửa hàng nào.'. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("POST /pos/api/preview: Thành công tính toán tạm tính và tiền thừa (mã 200)")
    void testPreviewSale_Success() throws Exception {
        PosSaleCommand cmd = PosSaleCommand.builder()
                .items(List.of(PosSaleItemCommand.builder().variantId(1L).quantity(2).build()))
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("1000000"))
                .build();

        PosSalePreviewResponseDTO previewDTO = PosSalePreviewResponseDTO.builder()
                .subtotal(new BigDecimal("1000000"))
                .discountTotal(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1000000"))
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("1000000"))
                .changeAmount(BigDecimal.ZERO)
                .totalQuantity(2)
                .build();

        when(posSaleService.previewSale(eq(10L), any(PosSaleCommand.class))).thenReturn(previewDTO);

        MvcResult result = mockMvc.perform(post("/pos/api/preview")
                        .principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.subtotal").value(1000000))
                .andExpect(jsonPath("$.data.totalAmount").value(1000000))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(200, result.getResponse().getStatus(),
                "Mã HTTP phải là 200 khi preview thành công. Thực tế: " + result.getResponse().getStatus() + ", Nội dung: " + responseBody);
        assertTrue(responseBody.contains("1000000"),
                "Nội dung phản hồi preview phải chứa tổng tiền 1000000. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("POST /pos/api/preview: Dữ liệu đầu vào sai (danh sách rỗng) bị từ chối 400")
    void testPreviewSale_EmptyItems_Rejected() throws Exception {
        PosSaleCommand cmd = PosSaleCommand.builder()
                .items(Collections.emptyList())
                .paymentMethod(PaymentMethod.CASH)
                .build();

        MvcResult result = mockMvc.perform(post("/pos/api/preview")
                        .principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(400, result.getResponse().getStatus(),
                "Mã HTTP phải là 400 khi giỏ hàng rỗng. Thực tế: " + result.getResponse().getStatus() + ", Nội dung: " + responseBody);
        assertTrue(responseBody.contains("Giỏ hàng không được để trống"),
                "Thông điệp phải từ chối giỏ hàng rỗng. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("POST /pos/api/preview: Dữ liệu đầu vào sai (số lượng 0) bị từ chối 400")
    void testPreviewSale_ZeroQuantity_Rejected() throws Exception {
        PosSaleCommand cmd = PosSaleCommand.builder()
                .items(List.of(PosSaleItemCommand.builder().variantId(1L).quantity(0).build()))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        MvcResult result = mockMvc.perform(post("/pos/api/preview")
                        .principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(400, result.getResponse().getStatus(),
                "Mã HTTP phải là 400 khi số lượng bằng 0. Thực tế: " + result.getResponse().getStatus() + ", Nội dung: " + responseBody);
        assertTrue(responseBody.contains("Số lượng mua mỗi sản phẩm phải từ 1 đến 20"),
                "Thông điệp phải cảnh báo số lượng từ 1 đến 20. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("POST /pos/api/checkout: Thành công trả mã đơn hàng để chuyển sang trang hóa đơn (mã 200)")
    void testCheckout_Success() throws Exception {
        PosSaleCommand cmd = PosSaleCommand.builder()
                .items(List.of(PosSaleItemCommand.builder().variantId(1L).quantity(1).build()))
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("500000"))
                .build();

        PosSaleResponseDTO responseDTO = PosSaleResponseDTO.builder()
                .orderId(100L)
                .orderCode("HD202611080001")
                .subtotal(new BigDecimal("500000"))
                .totalAmount(new BigDecimal("500000"))
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("500000"))
                .changeAmount(BigDecimal.ZERO)
                .placedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();

        when(posSaleService.checkout(eq(10L), any(PosSaleCommand.class))).thenReturn(responseDTO);

        MvcResult result = mockMvc.perform(post("/pos/api/checkout")
                        .principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderCode").value("HD202611080001"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(200, result.getResponse().getStatus(),
                "Mã HTTP phải là 200 khi checkout thành công. Thực tế: " + result.getResponse().getStatus() + ", Nội dung: " + responseBody);
        assertTrue(responseBody.contains("HD202611080001"),
                "Kết quả phản hồi phải chứa mã hóa đơn HD202611080001. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("POST /pos/api/checkout: Hết hàng tại chi nhánh -> Trả lỗi nghiệp vụ tiếng Việt (mã 400)")
    void testCheckout_InsufficientStock_ReturnsBadRequest() throws Exception {
        PosSaleCommand cmd = PosSaleCommand.builder()
                .items(List.of(PosSaleItemCommand.builder().variantId(1L).quantity(5).build()))
                .paymentMethod(PaymentMethod.CASH)
                .cashReceived(new BigDecimal("2500000"))
                .build();

        when(posSaleService.checkout(eq(10L), any(PosSaleCommand.class)))
                .thenThrow(new InsufficientStockException("Sản phẩm [Giày Sneaker - SNK-01-42] tại cửa hàng hiện đã hết hàng (tồn: 0)."));

        MvcResult result = mockMvc.perform(post("/pos/api/checkout")
                        .principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Sản phẩm [Giày Sneaker - SNK-01-42] tại cửa hàng hiện đã hết hàng (tồn: 0)."))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(400, result.getResponse().getStatus(),
                "Mã HTTP phải là 400 khi hết hàng. Thực tế: " + result.getResponse().getStatus() + ", Nội dung: " + responseBody);
        assertTrue(responseBody.contains("hết hàng (tồn: 0)"),
                "Thông điệp phải chứa nội dung 'hết hàng (tồn: 0)'. Thực tế: " + responseBody);
    }

    @Test
    @DisplayName("POST /pos/api/checkout: Dữ liệu đầu vào sai (danh sách rỗng) bị từ chối 400")
    void testCheckout_EmptyItems_Rejected() throws Exception {
        PosSaleCommand cmd = PosSaleCommand.builder()
                .items(Collections.emptyList())
                .paymentMethod(PaymentMethod.CARD)
                .build();

        MvcResult result = mockMvc.perform(post("/pos/api/checkout")
                        .principal(authPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cmd)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals(400, result.getResponse().getStatus(),
                "Mã HTTP phải là 400 khi danh sách rỗng. Thực tế: " + result.getResponse().getStatus() + ", Nội dung: " + responseBody);
        assertTrue(responseBody.contains("Giỏ hàng không được để trống"),
                "Thông điệp phải từ chối giỏ hàng rỗng. Thực tế: " + responseBody);
    }
}
