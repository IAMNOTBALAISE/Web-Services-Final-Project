package com.example.orderservices.datamapperlayer;


import com.example.orderservices.presentationlayer.OrderRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
//import org.springframework.core.annotation.Order;

import com.example.orderservices.dataaccesslayer.Order;


@Mapper(componentModel = "spring",
        imports = java.time.LocalDateTime.class)
public interface OrderRequestMapper {


    @Mapping(target = "id",                    ignore = true)
    @Mapping(target = "orderIdentifier",       ignore = true)
    @Mapping(target = "customerIdentifier",    ignore = true)
    @Mapping(target = "catalogIdentifier",     ignore = true)
    @Mapping(target = "watchIdentifier",       ignore = true)
    @Mapping(target = "servicePlanIdentifier", ignore = true)

    // we only carry over the name automatically
    @Mapping(target = "orderName", source = "orderName")

    // everything else (date, status, price, currency) we set in the service
    @Mapping(target = "orderStatus",  ignore = true)
    @Mapping(target = "orderDate",    ignore = true)
    @Mapping(target = "price",        ignore = true)
    @Mapping(target = "currency",     ignore = true)
    Order requestModelToEntity(OrderRequestModel dto);
}
