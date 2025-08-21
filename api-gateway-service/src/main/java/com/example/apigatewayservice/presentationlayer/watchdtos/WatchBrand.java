package com.example.apigatewayservice.presentationlayer.watchdtos;


import lombok.*;


@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class WatchBrand {

   // @Column(name = "brand_name",nullable = false)
    private String brandName;

  //  @Column(name = "brand_country",nullable = false)
    private String brandCountry;



}
