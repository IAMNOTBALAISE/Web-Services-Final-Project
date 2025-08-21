package com.example.apigatewayservice.presentationlayer.catalogdtos;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CatalogRequestModel {


    private String type;
    private String description;
}
