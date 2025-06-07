package com.yoyakso.comket.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    private String email;
    private String code;
    private String newPassword;
}