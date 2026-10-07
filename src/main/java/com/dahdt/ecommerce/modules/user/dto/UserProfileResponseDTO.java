package com.dahdt.ecommerce.modules.user.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileResponseDTO {
    private Long id;
    private Long userId;
    private String email;
    private String phone;
    private String fullName;
    private String avatarUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String status;
}
