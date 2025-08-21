package com.example.serviceplanservices.dataaccesslayer;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "service_plans")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServicePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private ServicePlanIdentifier servicePlanIdentifier;

    private String coverageDetails;

    private LocalDate expirationDate;
}
