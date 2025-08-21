package com.example.serviceplanservices.presentationlayer;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServicePlanResponseModel {

    private String planId;
    private String coverageDetails;
    private LocalDate expirationDate;
}
