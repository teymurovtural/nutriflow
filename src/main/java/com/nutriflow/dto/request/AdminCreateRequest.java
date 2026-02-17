package com.nutriflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateRequest {
    @NotBlank(message = "Ad mütləqdir")
    private String firstName;

    @NotBlank(message = "Soyad mütləqdir")
    private String lastName;

    @Email(message = "Email formatı düzgün deyil")
    @NotBlank(message = "Email mütləqdir")
    private String email;

    @NotBlank(message = "Şifrə mütləqdir")
    private String password;
}