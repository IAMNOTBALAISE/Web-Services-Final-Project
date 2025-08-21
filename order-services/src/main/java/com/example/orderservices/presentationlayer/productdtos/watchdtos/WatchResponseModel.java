package com.example.orderservices.presentationlayer.productdtos.watchdtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WatchResponseModel {


    private String watchId;
    private String catalogId;

    private Integer quantity;
//    private WatchStatus watchStatus;
    private UsageType usageType;

    private String model;
    private String material;

    private List<Accessory> accessories;
    private Price price;
    private WatchBrand watchBrand;

}
