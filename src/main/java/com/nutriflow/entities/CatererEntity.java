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
    private String name; // Şirkət adı

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address; // Şirkətin fiziki ünvanı

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.CATERER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CatererStatus status; // ACTIVE, INACTIVE

    // Bu Caterer-ə təhkim olunmuş istifadəçilər
    @Builder.Default
    @OneToMany(mappedBy = "caterer", fetch = FetchType.LAZY)
    private List<UserEntity> users = new ArrayList<>();

    // Bu Caterer-in həyata keçirməli olduğu çatdırılmalar
    @Builder.Default
    @OneToMany(mappedBy = "caterer", fetch = FetchType.LAZY)
    private List<DeliveryEntity> deliveries = new ArrayList<>();
}