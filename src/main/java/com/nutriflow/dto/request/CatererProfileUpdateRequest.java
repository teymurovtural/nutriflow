package com.nutriflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatererProfileUpdateRequest {

    @Size(min = 2, max = 100, message = "Şirkət adı 2-100 simvol aralığında olmalıdır")
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Telefon nömrəsi düzgün formatda deyil")
    private String phone;

    @Size(max = 255, message = "Ünvan 255 simvolu keçməməlidir")
    private String address;

    @Email(message = "Email formatı düzgün deyil")
    private String email;

    @Size(min = 8, message = "Şifrə ən azı 8 simvoldan ibarət olmalıdır")
    private String password;
}