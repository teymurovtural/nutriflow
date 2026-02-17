package com.nutriflow.entities;

import com.nutriflow.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "dietitians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietitianEntity extends BaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    // Login üçün şifrə
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "specialization")
    private String specialization;  // Məs: "Vegan diet specialist"

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.DIETITIAN;

    @Column(name = "is_active")
    private boolean isActive = true;

    // Dietitian-ın məsuləyyətində olan user-lər
    @OneToMany(mappedBy = "dietitian", fetch = FetchType.LAZY)
    private List<UserEntity> users;

    //  Yaratmış menüləri
    @OneToMany(mappedBy = "dietitian", cascade = CascadeType.ALL)
    private List<MenuEntity> menus;


}