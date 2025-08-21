package com.example.orderservices.businesslogiclayer;


import com.example.orderservices.presentationlayer.OrderRequestModel;
import com.example.orderservices.presentationlayer.OrderResponseModel;
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
