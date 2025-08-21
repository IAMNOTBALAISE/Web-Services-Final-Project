package com.example.productservices.dataccesslayer.watch;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Accessory {

    @Column(name = "accessory_name",nullable = false)
    private String accessoryName;

    @Column(name = "accessory_cost", nullable = false)
    private BigDecimal accessoryCost;


}
