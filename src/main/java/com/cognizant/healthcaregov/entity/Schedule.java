package com.cognizant.healthcaregov.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_schedule_doctor_date_slot",
                columnNames = {"doctorID", "available_date", "time_slot"}))
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer scheduleID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID", nullable = false)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospitalID", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private LocalDate availableDate;   // DB column: available_date

    @Column(nullable = false)
    private String timeSlot;           // DB column: time_slot

    @Column(nullable = false)
    private String status;             // Available | Booked
}
