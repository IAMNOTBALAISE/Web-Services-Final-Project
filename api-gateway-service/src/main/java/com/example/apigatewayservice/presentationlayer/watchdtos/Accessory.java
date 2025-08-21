package com.example.apigatewayservice.presentationlayer.watchdtos;



import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Accessory {

   // @Column(name = "accessory_name",nullable = false)
    private String accessoryName;

   // @Column(name = "accessory_cost", nullable = false)
    private BigDecimal accessoryCost;


}
