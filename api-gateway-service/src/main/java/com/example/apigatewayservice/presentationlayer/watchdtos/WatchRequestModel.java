package com.example.apigatewayservice.presentationlayer.watchdtos;

import lombok.*;

import java.util.List;

@Setter
@Getter

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchRequestModel {


    private String catalogId;

    private Integer quantity;
    private UsageType usageType;

    private String model;
    private String material;

    private List<Accessory> accessories;
    private Price price;
    private WatchBrand watchBrand;
}
