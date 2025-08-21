package com.example.apigatewayservice.presentationlayer;

import com.example.apigatewayservice.businesslayer.orderservicesBusinessLayer.OrderService;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderRequestModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseModel>> getAllOrders() {
        log.debug("API-Gateway ➜ GET all orders");
        List<OrderResponseModel> orders = orderService.getAllOrders();
        orders.forEach(this::addHateoasLinks);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> getOrderById(@PathVariable String orderId) {
        log.debug("API-Gateway ➜ GET order {}", orderId);
        OrderResponseModel order = orderService.getOrderById(orderId);
        addHateoasLinks(order);
        return ResponseEntity.ok(order);
    }

    @PostMapping
    public ResponseEntity<OrderResponseModel> createOrder(@RequestBody OrderRequestModel req) {
        log.debug("API-Gateway ➜ POST new order: {}", req.getOrderName());
        OrderResponseModel order = orderService.createOrder(req);
        addHateoasLinks(order);
        URI location = linkTo(methodOn(OrderController.class).getOrderById(order.getOrderId())).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponseModel> updateOrder(
            @PathVariable String orderId,
            @RequestBody OrderRequestModel req
    ) {
        log.debug("API-Gateway ➜ PUT order {} → {}", orderId, req.getOrderStatus());
        OrderResponseModel order = orderService.updateOrder(orderId, req);
        addHateoasLinks(order);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
        log.debug("API-Gateway ➜ DELETE order {}", orderId);
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    private void addHateoasLinks(OrderResponseModel o) {
        // self link
        o.add(linkTo(methodOn(OrderController.class)
                .getOrderById(o.getOrderId()))
                .withSelfRel());

        // customer
        o.add(Link.of("/api/v1/customers/" + o.getCustomerId(), "customer"));

        // catalog
        o.add(Link.of("/api/v1/catalogs/" + o.getCatalogId(), "catalog"));

        // watch in catalog
        o.add(Link.of(
                "/api/v1/catalogs/" +
                        o.getCatalogId() +
                        "/watches/" +
                        o.getWatchId(),
                "watch"));

        // service plan
        o.add(Link.of("/api/v1/plans/" + o.getServicePlanId(), "servicePlan"));
    }

}
