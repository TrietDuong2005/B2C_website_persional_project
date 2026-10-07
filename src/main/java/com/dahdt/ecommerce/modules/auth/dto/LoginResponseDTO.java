package com.dahdt.ecommerce.modules.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long userId;
}
