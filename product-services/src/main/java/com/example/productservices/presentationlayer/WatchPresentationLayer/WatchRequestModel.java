package com.example.productservices.presentationlayer.WatchPresentationLayer;

import com.example.productservices.dataccesslayer.watch.*;
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
    private UsageType usageType;
    private String model;
    private String material;
    private List<Accessory> accessories;
    private Price price;
    private WatchBrand watchBrand;
}
