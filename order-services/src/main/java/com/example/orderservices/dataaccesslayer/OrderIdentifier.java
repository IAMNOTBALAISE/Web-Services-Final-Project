package com.example.orderservices.dataaccesslayer;


import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class OrderIdentifier {


//    @Column(name = "order_id",nullable = false,unique = true)
    private String orderId;

    public OrderIdentifier() {
        this.orderId = UUID.randomUUID().toString();
    }



}
