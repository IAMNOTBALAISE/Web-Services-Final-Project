package com.example.orderservices.datamapperlayer;



import com.example.orderservices.dataaccesslayer.Order;
import com.example.orderservices.presentationlayer.OrderController;
import com.example.orderservices.presentationlayer.OrderResponseModel;
import com.example.orderservices.presentationlayer.customerdtos.CustomerResponseModel;
import com.example.orderservices.presentationlayer.productdtos.catalogdtos.CatalogResponseModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.WatchResponseModel;
import com.example.orderservices.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {

    /* ─────────── single-entity → DTO ─────────── */
    @Mapping(target = "orderId",
            expression = "java(order.getOrderIdentifier().getOrderId())")

    /* identifiers embedded in Order */
    @Mapping(target = "customerId",
            expression = "java(order.getCustomerIdentifier().getCustomerId())")
    @Mapping(target = "catalogId",
            expression = "java(order.getCatalogIdentifier().getCatalogId())")
    @Mapping(target = "watchId",
            expression = "java(order.getWatchIdentifier().getWatchId())")
    @Mapping(target = "servicePlanId",
            expression = "java(order.getServicePlanIdentifier().getPlanId())")

    /* price block */
    @Mapping(target = "salePrice",
            expression = "java(order.getPrice().getAmount().doubleValue())")
    @Mapping(target = "saleCurrency",
            expression = "java(order.getPrice().getCurrency().name())")
    @Mapping(target = "paymentCurrency",
            expression = "java(order.getPrice().getPaymentCurrency().name())")

    /* direct scalar fields */
    @Mapping(target = "orderName",   source = "orderName")
    @Mapping(target = "orderDate",   source = "orderDate")
    @Mapping(target = "orderStatus", source = "orderStatus")

    /* fields you don’t populate from Order → ignore */
    @Mapping(target = "customerFirstName",        ignore = true)
    @Mapping(target = "customerLastName",         ignore = true)
    @Mapping(target = "catalogType",              ignore = true)
    @Mapping(target = "catalogDescription",       ignore = true)
    @Mapping(target = "watchModel",               ignore = true)
    @Mapping(target = "watchMaterial",            ignore = true)
    @Mapping(target = "servicePlanCoverageDetails", ignore = true)
    @Mapping(target = "servicePlanExpirationDate",  ignore = true)

    OrderResponseModel entityToResponseModel(Order order);

    /* ─────────── list helper ─────────── */
    List<OrderResponseModel> entityToResponseModelList(List<Order> orders);


//    @AfterMapping
//    default void addLinks(@MappingTarget OrderResponseModel resp, Order order) {
//        // self
//        Link self = WebMvcLinkBuilder.linkTo(
//                        WebMvcLinkBuilder.methodOn(OrderController.class)
//                                .getOrderById(order.getOrderIdentifier().getOrderId()))
//                .withSelfRel();
//        resp.add(self);
//
//        // customer
//        resp.add(Link.of(
//                "/api/v1/customers/" + order.getCustomerIdentifier().getCustomerId(),
//                "customer"));
//
//        // catalog
//        resp.add(Link.of(
//                "/api/v1/catalogs/" + order.getCatalogIdentifier().getCatalogId(),
//                "catalog"));
//
//        // watch (in its catalog)
//        resp.add(Link.of(
//                "/api/v1/catalogs/" +
//                        order.getCatalogIdentifier().getCatalogId() +
//                        "/watches/" +
//                        order.getWatchIdentifier().getWatchId(),
//                "watch"));
//
//        // service plan
//        resp.add(Link.of(
//                "/api/v1/plans/" + order.getServicePlanIdentifier().getPlanId(),
//                "servicePlan"));
//    }
}


