package com.nutriflow.dto.request;

import com.nutriflow.enums.GoalType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    // --- Şəxsi məlumatlar ---
    @Size(min = 2, max = 50, message = "Ad 2-50 simvol aralığında olmalıdır")
    private String firstName;

    @Size(min = 2, max = 50, message = "Soyad 2-50 simvol aralığında olmalıdır")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Telefon nömrəsi düzgün formatda deyil (məs: +994501234567)")
    private String phoneNumber;

    @Size(min = 8, message = "Şifrə ən azı 8 simvoldan ibarət olmalıdır")
    private String password;

    // --- Ünvan məlumatları ---
    @Size(max = 100, message = "Şəhər adı çox uzundur")
    private String city;

    @Size(max = 100, message = "Rayon adı çox uzundur")
    private String district;

    @Size(max = 255, message = "Ünvan detalları 255 simvolu keçməməlidir")
    private String addressDetails;

    @Size(max = 500, message = "Çatdırılma qeydləri çox uzundur")
    private String deliveryNotes;

    // --- Sağlamlıq məlumatları ---
    @Positive(message = "Çəki müsbət rəqəm olmalıdır")
    @Max(value = 500, message = "Çəki miqdarı məntiqsizdir")
    private Double weight;

    @Positive(message = "Boy müsbət rəqəm olmalıdır")
    @Max(value = 300, message = "Boy miqdarı məntiqsizdir")
    private Double height;

    private GoalType goal; // Enum olduğu üçün adətən @NotNull kifayət edər (əgər məcburidirsə)

    @Size(max = 1000, message = "Məhdudiyyətlər qeydi 1000 simvolu keçməməlidir")
    private String restrictions;

    @Size(max = 2000, message = "Qeydlər 2000 simvolu keçməməlidir")
    private String notes;
}