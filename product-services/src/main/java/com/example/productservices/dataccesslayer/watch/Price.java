package com.example.productservices.dataccesslayer.watch;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Price {

    @Column(name = "msrp",nullable = false)
    private BigDecimal msrp;

    @Column(name = "cost",nullable = false)
    private BigDecimal cost;

    @Column(name = "total_options_cost",nullable = false)
    private BigDecimal totalOptionsCost;
}
