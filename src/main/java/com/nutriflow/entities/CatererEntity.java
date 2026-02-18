package com.nutriflow.entities;

import com.nutriflow.enums.CatererStatus;
import com.nutriflow.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caterers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatererEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name; // Company name

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // Company's physical address

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.CATERER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CatererStatus status; // ACTIVE, INACTIVE

    // Users assigned to this Caterer
    @Builder.Default
    @OneToMany(mappedBy = "caterer", fetch = FetchType.LAZY)
    private List<UserEntity> users = new ArrayList<>();

    // Deliveries this Caterer is responsible for
    @Builder.Default
    @OneToMany(mappedBy = "caterer", fetch = FetchType.LAZY)
    private List<DeliveryEntity> deliveries = new ArrayList<>();
}