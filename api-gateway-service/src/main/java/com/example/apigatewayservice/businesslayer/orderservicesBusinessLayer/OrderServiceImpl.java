package com.example.apigatewayservice.businesslayer.orderservicesBusinessLayer;


import com.example.apigatewayservice.domainclientlayer.OrderServiceClient;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderRequestModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderResponseModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {


    private final OrderServiceClient orderServiceClient;

    public OrderServiceImpl(
           OrderServiceClient orderServiceClient
    ) {
       this.orderServiceClient = orderServiceClient;
    }

    @Override
    public List<OrderResponseModel> getAllOrders() {

        return this.orderServiceClient.getAllOrders();
    }

    @Override
    public OrderResponseModel getOrderById(String orderId) {

        return this.orderServiceClient.getOrderById(orderId);

    }

    @Override
    public OrderResponseModel createOrder(OrderRequestModel req) {

        return this.orderServiceClient.createOrder(req);
    }
    @Override
    public OrderResponseModel updateOrder(String orderId, OrderRequestModel req) {

        return this.orderServiceClient.updateOrder(orderId, req);

    }

    @Override
    public String deleteOrder(String orderId) {

return this.orderServiceClient.deleteOrder(orderId);
    }



}
