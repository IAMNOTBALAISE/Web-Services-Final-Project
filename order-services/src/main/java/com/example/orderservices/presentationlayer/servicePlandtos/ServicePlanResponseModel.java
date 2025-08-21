package com.example.orderservices.presentationlayer.servicePlandtos;


import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePlanResponseModel {


    private String planId;
    private String coverageDetails;
    private LocalDate expirationDate;

}
