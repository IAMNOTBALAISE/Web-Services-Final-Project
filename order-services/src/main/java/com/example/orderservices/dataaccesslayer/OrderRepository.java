package com.example.orderservices.dataaccesslayer;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, Integer> {

    Order findOrderByOrderIdentifier_OrderId(String orderId);

    boolean existsByOrderIdentifier_OrderId(String orderId);

    boolean existsByWatchIdentifier_WatchId(String watchId);

    boolean existsByOrderName(String orderName);

    boolean existsByWatchIdentifier_WatchIdAndOrderStatus(String watchId, OrderStatus orderStatus);
}
