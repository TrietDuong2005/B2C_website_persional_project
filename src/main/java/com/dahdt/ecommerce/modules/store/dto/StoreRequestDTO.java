package com.dahdt.ecommerce.modules.store.dto;

import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO nhận dữ liệu gửi lên từ form khi tạo mới hoặc cập nhật cửa hàng / kho.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRequestDTO {

    private Long id;

    @NotBlank(message = "Mã chi nhánh không được để trống")
    @Size(max = 50, message = "Mã chi nhánh tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Mã chi nhánh chỉ được chứa chữ cái, số, gạch nối (-) hoặc gạch dưới (_)")
    private String code;

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 255, message = "Tên chi nhánh tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Vui lòng chọn loại điểm (Cửa hàng / Kho)")
    @Builder.Default
    private StoreType type = StoreType.STORE;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh/Thành phố tối đa 100 ký tự")
    private String province;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Quận/Huyện tối đa 100 ký tự")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 100, message = "Phường/Xã tối đa 100 ký tự")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 50, message = "Địa chỉ chi tiết tối đa 500 ký tự")
    private String detailAddress;

    @DecimalMin(value = "-90.0", message = "Vĩ độ (Latitude) phải từ -90 đến 90")
    @DecimalMax(value = "90.0", message = "Vĩ độ (Latitude) phải từ -90 đến 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Kinh độ (Longitude) phải từ -180 đến 180")
    @DecimalMax(value = "180.0", message = "Kinh độ (Longitude) phải từ -180 đến 180")
    private BigDecimal longitude;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Builder.Default
    private StoreStatus status = StoreStatus.ACTIVE;
}

