package com.example.orderservices.dataaccesslayer;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;


@Data
@Builder
@Document(collection = "orders")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Order {


    @Id
    private String id;


    private OrderIdentifier orderIdentifier;

    private String orderName;


    private CustomerIdentifier customerIdentifier;


    private WatchIdentifier watchIdentifier;


    private ServicePlanIdentifier servicePlanIdentifier;


    private CatalogIdentifier catalogIdentifier;



    private OrderStatus orderStatus;


    private Price price;


    private Currency currency;

    private LocalDateTime orderDate;

}
