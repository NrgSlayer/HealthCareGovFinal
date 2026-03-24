package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer patientID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userID")
    private User user; // nullable – patient may exist without a login account

    @Column(nullable = false)
    private String name;

    private LocalDate dob;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String contactInfo;

    @Column(nullable = false)
    private String status; // Active | Inactive | Finalized
}
