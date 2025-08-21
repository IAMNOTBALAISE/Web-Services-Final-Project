package com.example.apigatewayservice.presentationlayer.orderdtos;


import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class OrderResponseModel extends RepresentationModel<OrderResponseModel> {

    /* ─────────────── core identifiers ─────────────── */
    private String orderId;

    // Customer
    private String customerId;
    private String customerFirstName;
    private String customerLastName;

    // Catalog
    private String catalogId;
    private String catalogType;
    private String catalogDescription;

    // Watch
    private String watchId;
    private String watchModel;
    private String watchMaterial;

    /* ─────────────── service-plan (flattened) ─────────────── */
    private String servicePlanId;
    private String servicePlanCoverageDetails;
    private LocalDate servicePlanExpirationDate;

    /* ─────────────── order specifics ─────────────── */
    private String  orderName;
    private Double  salePrice;
    private String  saleCurrency;
    private String  paymentCurrency;
    private LocalDateTime orderDate;
    private OrderStatus   orderStatus;

}
