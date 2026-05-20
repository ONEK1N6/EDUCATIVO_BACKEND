package com.educativo.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "fee_configs")
public class FeeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private EducationLevel level;

    @Column(nullable = false)
    private Double monthlyAmount;

    @Column(nullable = false)
    private Double registrationAmount;

    @Column(nullable = false)
    private Integer dueDay;

    private LocalDateTime lastUpdated;
}
