package com.nutriflow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "address_details", nullable = false, columnDefinition = "TEXT")
    private String addressDetails; // Full street, building, apartment details

    @Column(name = "city", nullable = false)
    private String city; // City (e.g: Baku)

    @Column(name = "district", nullable = false)
    private String district; // District (e.g: Nasimi)

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes; // Special notes for the courier
}