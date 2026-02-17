package com.nutriflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyRequest {

    @Email(message = "Email formatı düzgün deyil")
    @NotBlank(message = "Email boş ola bilməz")
    private String email;

    @NotBlank(message = "OTP kodu boş ola bilməz")
    @Size(min = 6, max = 6, message = "OTP kodu tam olaraq 6 rəqəmdən ibarət olmalıdır")
    @Pattern(regexp = "^[0-9]+$", message = "OTP kodu yalnız rəqəmlərdən ibarət olmalıdır")
    private String otpCode;
}