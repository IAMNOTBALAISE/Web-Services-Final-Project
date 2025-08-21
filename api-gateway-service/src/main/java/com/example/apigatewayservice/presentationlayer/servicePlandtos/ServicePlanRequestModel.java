package com.example.apigatewayservice.presentationlayer.servicePlandtos;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServicePlanRequestModel {

//    private String planId;
    private String coverageDetails;
    private LocalDate expirationDate;
}
