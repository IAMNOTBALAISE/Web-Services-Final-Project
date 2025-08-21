package com.example.apigatewayservice.presentationlayer.servicePlandtos;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServicePlanResponseModel extends RepresentationModel<ServicePlanResponseModel> {

    private String planId;
    private String coverageDetails;
    private LocalDate expirationDate;
}
