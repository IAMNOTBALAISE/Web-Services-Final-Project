package com.example.orderservices.presentationlayer.productdtos.watchdtos;



import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Price {


    private BigDecimal msrp;


    private BigDecimal cost;


    private BigDecimal totalOptionsCost;
}
