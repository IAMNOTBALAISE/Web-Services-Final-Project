package com.example.apigatewayservice.presentationlayer.orderdtos;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
public class OrderRequestModel {


    private String customerId;
    private String catalogId;
    private String watchId;
    private String servicePlanId;
    private String orderName;
    private Double salePrice;
    private String currency;
    private String paymentCurrency;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
}
