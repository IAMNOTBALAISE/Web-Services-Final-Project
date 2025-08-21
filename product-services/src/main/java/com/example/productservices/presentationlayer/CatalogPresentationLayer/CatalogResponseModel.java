package com.example.productservices.presentationlayer.CatalogPresentationLayer;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CatalogResponseModel {

    private String catalogId;
    private String type;
    private String description;
}
