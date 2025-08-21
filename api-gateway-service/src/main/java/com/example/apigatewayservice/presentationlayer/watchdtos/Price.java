package com.example.apigatewayservice.presentationlayer.watchdtos;



import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Price {

   // @Column(name = "msrp",nullable = false)
    private BigDecimal msrp;

  //  @Column(name = "cost",nullable = false)
    private BigDecimal cost;

   // @Column(name = "total_options_cost",nullable = false)
    private BigDecimal totalOptionsCost;
}
