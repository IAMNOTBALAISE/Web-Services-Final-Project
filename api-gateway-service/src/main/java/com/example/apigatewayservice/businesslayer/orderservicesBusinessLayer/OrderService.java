package com.example.apigatewayservice.businesslayer.orderservicesBusinessLayer;



import com.example.apigatewayservice.presentationlayer.orderdtos.OrderRequestModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {

    List<OrderResponseModel> getAllOrders();

    OrderResponseModel getOrderById(String orderId);

    OrderResponseModel createOrder(OrderRequestModel orderRequestModel);

    OrderResponseModel updateOrder(String orderId, OrderRequestModel orderRequestModel);

    String deleteOrder(String orderId);
}
