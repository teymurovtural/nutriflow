package com.nutriflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietitianUpdateRequest {

    @Size(min = 2, max = 50, message = "Ad 2-50 simvol aralığında olmalıdır")
    private String firstName;

    @Size(min = 2, max = 50, message = "Soyad 2-50 simvol aralığında olmalıdır")
    private String lastName;

    @Email(message = "Email formatı düzgün deyil")
    private String email;

    @Size(min = 8, message = "Şifrə ən azı 8 simvoldan ibarət olmalıdır")
    private String password;

    @Size(max = 100, message = "İxtisas sahəsi 100 simvolu keçməməlidir")
    private String specialization;
}