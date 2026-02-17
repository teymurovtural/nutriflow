package com.nutriflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileUpdateRequest {

    @NotBlank(message = "Ad boş ola bilməz")
    private String firstName;

    @NotBlank(message = "Soyad boş ola bilməz")
    private String lastName;

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Düzgün email formatı daxil edin")
    private String email;

    // Əgər parol yazılacaqsa, ən az 8 simvol olmalıdır
    @Size(min = 8, message = "Yeni parol ən az 8 simvoldan ibarət olmalıdır")
    private String newPassword;
}