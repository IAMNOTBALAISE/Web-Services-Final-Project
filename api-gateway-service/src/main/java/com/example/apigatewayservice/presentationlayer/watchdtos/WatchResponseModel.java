package com.example.apigatewayservice.presentationlayer.watchdtos;

import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchResponseModel extends RepresentationModel<WatchResponseModel> {

    private String watchId;
    private String catalogId;
    private Integer quantity;
    private UsageType usageType;
    private String model;
    private String material;
    private List<Accessory> accessories;
    private Price price;
    private WatchBrand watchBrand;
}
