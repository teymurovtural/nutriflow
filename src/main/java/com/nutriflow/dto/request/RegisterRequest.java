package com.nutriflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Ad mütləqdir")
    @Size(min = 2, max = 50, message = "Ad 2-50 simvol aralığında olmalıdır")
    private String firstName;

    @NotBlank(message = "Soyad mütləqdir")
    @Size(min = 2, max = 50, message = "Soyad 2-50 simvol aralığında olmalıdır")
    private String lastName;

    @Email(message = "Email formatı düzgün deyil")
    @NotBlank(message = "Email mütləqdir")
    @Size(max = 100, message = "Email çox uzundur")
    private String email;

    @NotBlank(message = "Şifrə mütləqdir")
    @Size(min = 8, message = "Şifrə ən az 8 simvol olmalıdır")
    private String password;

    @NotBlank(message = "Şifrənin təkrarı mütləqdir")
    private String confirmPassword;

    @NotBlank(message = "Telefon nömrəsi mütləqdir")
    @Pattern(regexp = "^\\+994(50|51|55|70|77|99|10)\\d{7}$", message = "Düzgün Azərbaycan nömrəsi daxil edin")
    private String phoneNumber;

}