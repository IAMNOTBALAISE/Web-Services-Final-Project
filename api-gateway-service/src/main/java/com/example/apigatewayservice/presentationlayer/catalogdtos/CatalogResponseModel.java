package com.example.apigatewayservice.presentationlayer.catalogdtos;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CatalogResponseModel extends RepresentationModel<CatalogResponseModel> {

    private String catalogId;
    private String type;
    private String description;
}
