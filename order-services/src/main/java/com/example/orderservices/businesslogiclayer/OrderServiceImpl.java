package com.example.orderservices.businesslogiclayer;


import com.example.orderservices.dataaccesslayer.*;
import com.example.orderservices.datamapperlayer.OrderRequestMapper;
import com.example.orderservices.datamapperlayer.OrderResponseMapper;
import com.example.orderservices.domainclientlayer.CustomerServiceClient;
import com.example.orderservices.domainclientlayer.ProductServiceClient;
import com.example.orderservices.domainclientlayer.ServicePlanServiceClient;
import com.example.orderservices.presentationlayer.OrderRequestModel;
import com.example.orderservices.presentationlayer.OrderResponseModel;
import com.example.orderservices.presentationlayer.customerdtos.CustomerResponseModel;
import com.example.orderservices.presentationlayer.productdtos.catalogdtos.CatalogResponseModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.WatchRequestModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.WatchResponseModel;
import com.example.orderservices.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.example.orderservices.utils.DuplicateOrderName;
import com.example.orderservices.utils.InvalidInputException;
import com.example.orderservices.utils.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderRequestMapper orderRequestMapper;
    private final OrderResponseMapper orderResponseMapper;
    private final CustomerServiceClient customerClient;
    private final ProductServiceClient productClient;
    private final ServicePlanServiceClient planClient;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderRequestMapper orderRequestMapper,
            OrderResponseMapper orderResponseMapper,
            CustomerServiceClient customerClient,
            ProductServiceClient productClient,
            ServicePlanServiceClient planClient
    ) {
        this.orderRepository = orderRepository;
        this.orderRequestMapper = orderRequestMapper;
        this.orderResponseMapper = orderResponseMapper;
        this.customerClient = customerClient;
        this.productClient = productClient;
        this.planClient = planClient;
    }

    @Override
    public List<OrderResponseModel> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponseModel> results = new ArrayList<>();

        for (Order o : orders) {
            String id = o.getOrderIdentifier().getOrderId();

            CustomerResponseModel    c;
            CatalogResponseModel     d;
            WatchResponseModel       w;
            ServicePlanResponseModel p;

            // 1) do your 4 downstream calls inside a try/catch
            try {
                c = customerClient.getCustomerbyCustomerId(o.getCustomerIdentifier().getCustomerId());
                d = productClient  .getCatalogById   (o.getCatalogIdentifier().getCatalogId());
                w = productClient  .getCatalogWatchByID(o.getWatchIdentifier().getWatchId());
                p = planClient     .getServicePlansById(o.getServicePlanIdentifier().getPlanId());
            } catch (RuntimeException ex) {
                // upstream threw 404 or 500 → delete stale order + skip
                orderRepository.delete(o);
                continue;
            }

            // 2) if any lookup returned null, also cascade-delete
            if (c == null || d == null || w == null || p == null) {
                orderRepository.delete(o);
                continue;
            }

            // 3) map the entity → DTO, then enrich it
            OrderResponseModel dto = orderResponseMapper.entityToResponseModel(o);
            enrichResponse(dto);

            results.add(dto);
        }

        return results;
    }


    @Override
    public OrderResponseModel getOrderById(String orderId) {
        Order o = orderRepository.findOrderByOrderIdentifier_OrderId(orderId);
        if (o == null) {
            throw new NotFoundException("Order ID '" + orderId + "' not found");
        }

        try {
            // reuse your existing enrichResponse inside this try
            CustomerResponseModel    c = customerClient.getCustomerbyCustomerId(o.getCustomerIdentifier().getCustomerId());
            CatalogResponseModel     d = productClient  .getCatalogById   (o.getCatalogIdentifier().getCatalogId());
            WatchResponseModel       w = productClient  .getCatalogWatchByID(o.getWatchIdentifier().getWatchId());
            ServicePlanResponseModel p = planClient     .getServicePlansById(o.getServicePlanIdentifier().getPlanId());

            if (c == null || d == null || w == null || p == null) {
                throw new RuntimeException("stale downstream resource");
            }

            OrderResponseModel dto = orderResponseMapper.entityToResponseModel(o);
            enrichResponse(dto);
            return dto;

        } catch (RuntimeException ex) {
            orderRepository.delete(o);
            throw new NotFoundException(
                    "Order ID '" + orderId + "' referred to missing resource and has been removed"
            );
        }
    }
    @Override
    public OrderResponseModel createOrder(OrderRequestModel req) {
        // 1) required fields
        if (req.getOrderName() == null) {
            throw new InvalidInputException("orderName is required");
        }
        if (orderRepository.existsByOrderName(req.getOrderName())) {
            throw new DuplicateOrderName(req.getOrderName());
        }
        if (req.getCustomerId() == null) {
            throw new InvalidInputException("customerId is required");
        }
        if (req.getWatchId() == null) {
            throw new InvalidInputException("watchId is required");
        }
        if (req.getServicePlanId() == null) {
            throw new InvalidInputException("servicePlanId is required");
        }
        if (req.getSalePrice() == null) {
            throw new InvalidInputException("salePrice is required");
        }
        if (req.getCurrency() == null || req.getPaymentCurrency() == null) {
            throw new InvalidInputException("currency and paymentCurrency are required");
        }

        // 2) lookup customer
        CustomerResponseModel customer = customerClient.getCustomerbyCustomerId(req.getCustomerId());
        if (customer == null) {
            throw new NotFoundException("Customer ID '" + req.getCustomerId() + "' not found");
        }

        // 3) lookup watch
        WatchResponseModel watch = productClient.getCatalogWatchByID(req.getWatchId());
        if (watch == null) {
            throw new NotFoundException("Watch ID '" + req.getWatchId() + "' not found");
        }
        // 3.a) ensure watch belongs to the catalog
        if (! watch.getCatalogId().equals(req.getCatalogId())) {
            throw new InvalidInputException(
                    "Watch '" + req.getWatchId() +
                            "' does not belong to catalog '" + req.getCatalogId() + "'"
            );
        }

        if (watch.getQuantity() <= 0 && req.getOrderStatus() != OrderStatus.PURCHASE_CANCELED) {
            throw new InvalidInputException("Watch '" + req.getWatchId() + "' is out of stock");
        }


        ServicePlanResponseModel plan = planClient.getServicePlansById(req.getServicePlanId());
        if (plan == null) {
            throw new NotFoundException("Service Plan ID '" + req.getServicePlanId() + "' not found");
        }

        // 5) build entity
        Order order = orderRequestMapper.requestModelToEntity(req);
        order.setOrderIdentifier(new OrderIdentifier(java.util.UUID.randomUUID().toString()));
        order.setCustomerIdentifier(new CustomerIdentifier(req.getCustomerId()));
        order.setCatalogIdentifier(new CatalogIdentifier(req.getCatalogId()));
        order.setWatchIdentifier(new WatchIdentifier(req.getWatchId()));
        order.setServicePlanIdentifier(new ServicePlanIdentifier(req.getServicePlanId()));
        order.setOrderName(req.getOrderName());
        order.setOrderDate(LocalDateTime.now());

        OrderStatus status = req.getOrderStatus();
        if (status != OrderStatus.PURCHASE_CANCELED) {
            status = OrderStatus.PURCHASE_COMPLETED;
        }
        order.setOrderStatus(status);


        order.setPrice(new Price(
                BigDecimal.valueOf(req.getSalePrice()),
                Currency.valueOf(req.getCurrency().trim()),
                Currency.valueOf(req.getPaymentCurrency().trim())
        ));


        Order saved = orderRepository.save(order);

        int delta = saved.getOrderStatus() == OrderStatus.PURCHASE_COMPLETED ? -1 : +1;
        int newQty = watch.getQuantity() + delta;
        if (newQty < 0) {
            throw new InvalidInputException("Stock cannot go below zero");
        }
        WatchRequestModel upd = WatchRequestModel.builder()
                .catalogId(watch.getCatalogId())
                .quantity(newQty)
                .usageType(watch.getUsageType())
                .model(watch.getModel())
                .material(watch.getMaterial())
                .accessories(watch.getAccessories())
                .price(watch.getPrice())
                .watchBrand(watch.getWatchBrand())
                .build();
        productClient.updateWatchInInventory(watch.getCatalogId(), watch.getWatchId(), upd);

        // 10) build response
        OrderResponseModel dto = orderResponseMapper.entityToResponseModel(saved);
        enrichResponse(dto);
        return dto;
    }
    @Override
    public OrderResponseModel updateOrder(String orderId, OrderRequestModel req) {

        Order existing = orderRepository.findOrderByOrderIdentifier_OrderId(orderId);
        if (existing == null) {
            throw new NotFoundException("Order ID '" + orderId + "' not found");
        }


        if (req.getOrderName() == null || !req.getOrderName().equals(existing.getOrderName())) {
            throw new InvalidInputException(
                    "orderName must remain '" + existing.getOrderName() + "' for ID '" + orderId + "'"
            );
        }


        OrderStatus origStatus = existing.getOrderStatus();
        OrderStatus requested = req.getOrderStatus() == OrderStatus.PURCHASE_CANCELED
                ? OrderStatus.PURCHASE_CANCELED
                : OrderStatus.PURCHASE_COMPLETED;
        existing.setOrderStatus(requested);


        if (req.getOrderDate() != null) {
            existing.setOrderDate(req.getOrderDate());
        }
        if (req.getSalePrice() != null) {
            existing.setPrice(new Price(
                    BigDecimal.valueOf(req.getSalePrice()),
                    Currency.valueOf(req.getCurrency().trim()),
                    Currency.valueOf(req.getPaymentCurrency().trim())
            ));
        }


        Order saved = orderRepository.save(existing);


        int delta = 0;
        if (origStatus != OrderStatus.PURCHASE_COMPLETED
                && saved.getOrderStatus() == OrderStatus.PURCHASE_COMPLETED) {
            delta = -1;
        } else if (origStatus != OrderStatus.PURCHASE_CANCELED
                && saved.getOrderStatus() == OrderStatus.PURCHASE_CANCELED) {
            delta = +1;
        }

        if (delta != 0) {

            String watchId   = existing.getWatchIdentifier().getWatchId();
            String catalogId = existing.getCatalogIdentifier().getCatalogId();

            WatchResponseModel watch = productClient.getCatalogWatchByID(watchId);
            int updatedQty = watch.getQuantity() + delta;
            if (updatedQty < 0) {
                throw new InvalidInputException("Stock cannot go below zero");
            }

            WatchRequestModel upd = WatchRequestModel.builder()
                    .catalogId(catalogId)
                    .quantity(updatedQty)
                    .usageType(watch.getUsageType())
                    .model(watch.getModel())
                    .material(watch.getMaterial())
                    .accessories(watch.getAccessories())
                    .price(watch.getPrice())
                    .watchBrand(watch.getWatchBrand())
                    .build();

            productClient.updateWatchInInventory(catalogId, watchId, upd);
        }


        OrderResponseModel dto = orderResponseMapper.entityToResponseModel(saved);
        enrichResponse(dto);
        return dto;
    }

    @Override
    public String deleteOrder(String orderId) {

        Order order = orderRepository.findOrderByOrderIdentifier_OrderId(orderId);
        if (order == null) {
            throw new NotFoundException("Order ID '" + orderId + "' not found");
        }

        if (order.getOrderStatus() == OrderStatus.PURCHASE_COMPLETED) {
            WatchResponseModel watch = productClient.getCatalogWatchByID(
                    order.getWatchIdentifier().getWatchId()
            );
            int restoredQty = watch.getQuantity() + 1;
            WatchRequestModel upd = WatchRequestModel.builder()
                    .catalogId(watch.getCatalogId())
                    .quantity(restoredQty)
                    .usageType(watch.getUsageType())
                    .model(watch.getModel())
                    .material(watch.getMaterial())
                    .accessories(watch.getAccessories())
                    .price(watch.getPrice())
                    .watchBrand(watch.getWatchBrand())
                    .build();
            productClient.updateWatchInInventory(watch.getCatalogId(), watch.getWatchId(), upd);
        }


        orderRepository.delete(order);

        return "Order '" + orderId + "' deleted"
                + (order.getOrderStatus() == OrderStatus.PURCHASE_COMPLETED
                ? "; stock restored."
                : ".");
    }

    // ────── populate HATEOAS links, names, etc. ──────

    private void enrichResponse(OrderResponseModel r) {
        CustomerResponseModel    c = customerClient.getCustomerbyCustomerId(r.getCustomerId());
        CatalogResponseModel     d = productClient.getCatalogById(r.getCatalogId());
        WatchResponseModel       w = productClient.getCatalogWatchByID(r.getWatchId());
        ServicePlanResponseModel p = planClient.getServicePlansById(r.getServicePlanId());

        r.setCustomerFirstName(c.getFirstName());
        r.setCustomerLastName (c.getLastName());
        r.setCatalogType      (d.getType());
        r.setCatalogDescription(d.getDescription());
        r.setWatchModel       (w.getModel());
        r.setWatchMaterial    (w.getMaterial());
        r.setServicePlanCoverageDetails(p.getCoverageDetails());
        r.setServicePlanExpirationDate(p.getExpirationDate());
    }

}
