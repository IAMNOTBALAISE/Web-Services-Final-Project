package com.example.orderservices.presentationlayer.productdtos.watchdtos;


import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class WatchRequestModel {

    private String catalogId;
    private Integer quantity;
    private UsageType  usageType;
    private String model;
    private String material;
    private List<Accessory> accessories;
    private Price price;
    private WatchBrand watchBrand;
}
