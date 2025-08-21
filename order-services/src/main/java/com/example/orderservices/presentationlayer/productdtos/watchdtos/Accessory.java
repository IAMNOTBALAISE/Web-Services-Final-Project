package com.example.orderservices.presentationlayer.productdtos.watchdtos;


import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Accessory {


    private String accessoryName;


    private BigDecimal accessoryCost;


}
