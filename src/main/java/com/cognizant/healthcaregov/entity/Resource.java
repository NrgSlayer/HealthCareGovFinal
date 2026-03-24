package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "resource")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resourceID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospitalID", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String status;
}
