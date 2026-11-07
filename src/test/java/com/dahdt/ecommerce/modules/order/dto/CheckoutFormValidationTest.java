package com.dahdt.ecommerce.modules.order.dto;

import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Bean Validation cho Form nhận hàng (CheckoutForm)")
class CheckoutFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CheckoutForm createValidForm() {
        return CheckoutForm.builder()
                .shipName("Nguyễn Văn A")
                .shipPhone("0901234567")
                .province("Thành phố Hồ Chí Minh")
                .district("Quận 1")
                .ward("Phường Bến Thành")
                .detailAddress("123 Nguyễn Huệ")
                .paymentMethod(PaymentMethod.COD)
                .build();
    }

    @Test
    @DisplayName("Form hợp lệ đầy đủ thông tin: Không có lỗi validation")
    void testValidForm_NoViolations() {
        CheckoutForm form = createValidForm();
        Set<ConstraintViolation<CheckoutForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty(), "Form đầy đủ thông tin chuẩn không được có lỗi");
    }

    @Test
    @DisplayName("Họ tên người nhận bị rỗng: Báo lỗi bắt buộc")
    void testBlankShipName_Violation() {
        CheckoutForm form = createValidForm();
        form.setShipName("");

        Set<ConstraintViolation<CheckoutForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("shipName")));

        form.setShipName("   ");
        violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("shipName")));
    }

    @Test
    @DisplayName("Các trường địa chỉ bắt buộc bị rỗng: Báo lỗi bắt buộc tương ứng")
    void testBlankAddressFields_Violations() {
        CheckoutForm form = createValidForm();
        form.setProvince("");
        form.setDistrict("");
        form.setWard("");
        form.setDetailAddress("");

        Set<ConstraintViolation<CheckoutForm>> violations = validator.validate(form);
        assertEquals(4, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("province")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("district")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("ward")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("detailAddress")));
    }

    @Test
    @DisplayName("Số điện thoại hợp lệ (10 chữ số bắt đầu bằng 0): Pass validation")
    void testValidPhoneNumber_Success() {
        String[] validPhones = {"0901234567", "0381234567", "0987654321", "0701122334"};
        for (String phone : validPhones) {
            CheckoutForm form = createValidForm();
            form.setShipPhone(phone);
            Set<ConstraintViolation<CheckoutForm>> violations = validator.validate(form);
            assertTrue(violations.isEmpty(), "Số điện thoại [" + phone + "] phải hợp lệ");
        }
    }

    @Test
    @DisplayName("Số điện thoại sai định dạng: Báo lỗi chính xác")
    void testInvalidPhoneNumber_Violations() {
        String[] invalidPhones = {
                "1234567890", // Không bắt đầu bằng số 0
                "090123456",   // 9 chữ số (thiếu)
                "09012345678", // 11 chữ số (thừa)
                "090123456a", // Chứa ký tự chữ
                "+8490123456",// Chứa ký tự đặc biệt / mã vùng
                "",           // Rỗng
                "   "         // Khoảng trắng
        };

        for (String phone : invalidPhones) {
            CheckoutForm form = createValidForm();
            form.setShipPhone(phone);
            Set<ConstraintViolation<CheckoutForm>> violations = validator.validate(form);
            assertFalse(violations.isEmpty(), "Số điện thoại [" + phone + "] phải bị từ chối");
            assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("shipPhone")));
        }
    }

    @Test
    @DisplayName("buildFullAddress và toCommand hoạt động chính xác")
    void testBuildFullAddressAndToCommand() {
        CheckoutForm form = createValidForm();
        String fullAddress = form.buildFullAddress();
        assertEquals("123 Nguyễn Huệ, Phường Bến Thành, Quận 1, Thành phố Hồ Chí Minh", fullAddress);

        PlaceOrderCommand command = form.toCommand();
        assertEquals("Nguyễn Văn A", command.getShipName());
        assertEquals("0901234567", command.getShipPhone());
        assertEquals(fullAddress, command.getShipAddress());
        assertEquals(PaymentMethod.COD, command.getPaymentMethod());
    }
}

