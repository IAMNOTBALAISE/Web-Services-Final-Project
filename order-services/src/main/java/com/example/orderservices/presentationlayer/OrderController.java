package com.example.orderservices.presentationlayer;


import com.example.orderservices.businesslogiclayer.OrderService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping()
    public ResponseEntity<List<OrderResponseModel>> getAllOrders() {

        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }


    @PostMapping()
    public ResponseEntity<OrderResponseModel> createOrder(@RequestBody OrderRequestModel orderRequestModel) {
        OrderResponseModel createdOrder = orderService.createOrder(orderRequestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> updateOrder(@PathVariable String orderId, @RequestBody OrderRequestModel orderRequestModel) {
        OrderResponseModel updatedOrder = orderService.updateOrder(orderId, orderRequestModel);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order with ID: " + orderId + " deleted successfully.");
    }

}
