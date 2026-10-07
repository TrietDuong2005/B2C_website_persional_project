package com.dahdt.ecommerce.modules.user.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressResponseDTO {
    private Long id;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
    private boolean isDefault;
    private String type;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
