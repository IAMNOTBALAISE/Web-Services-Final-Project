package com.example.orderservices.businesslayer;

import com.example.orderservices.businesslogiclayer.OrderServiceImpl;
import com.example.orderservices.dataaccesslayer.*;
import com.example.orderservices.dataaccesslayer.Currency;
import com.example.orderservices.datamapperlayer.OrderRequestMapper;
import com.example.orderservices.datamapperlayer.OrderResponseMapper;
import com.example.orderservices.domainclientlayer.CustomerServiceClient;
import com.example.orderservices.domainclientlayer.ProductServiceClient;
import com.example.orderservices.domainclientlayer.ServicePlanServiceClient;
import com.example.orderservices.presentationlayer.OrderRequestModel;
import com.example.orderservices.presentationlayer.OrderResponseModel;
import com.example.orderservices.presentationlayer.customerdtos.CustomerResponseModel;
import com.example.orderservices.presentationlayer.productdtos.catalogdtos.CatalogResponseModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.UsageType;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.WatchRequestModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.WatchResponseModel;
import com.example.orderservices.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.example.orderservices.dataaccesslayer.Price;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.WatchBrand;

import com.example.orderservices.dataaccesslayer.Currency;
import com.example.orderservices.utils.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderRequestMapper requestMapper;

    @Mock
    OrderResponseMapper responseMapper;
    @Mock
    CustomerServiceClient customerClient;
    @Mock
    ProductServiceClient productClient;
    @Mock
    ServicePlanServiceClient planClient;

    @InjectMocks
    OrderServiceImpl service;

    // helper to build a dummy Order entity
    private Order makeOrder(String id) {
        Order o = new Order();
        o.setOrderIdentifier(new OrderIdentifier(id));
        o.setCustomerIdentifier(new CustomerIdentifier("C1"));
        o.setCatalogIdentifier(new CatalogIdentifier("CAT1"));
        o.setWatchIdentifier(new WatchIdentifier("W1"));
        o.setServicePlanIdentifier(new ServicePlanIdentifier("P1"));
        o.setOrderName("O1");
        o.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);


        o.setPrice(new Price(
                new BigDecimal("100"),
                Currency.valueOf("USD"),             // saleCurrency
                Currency.valueOf("EUR")
        ));
        o.setOrderDate(LocalDateTime.now());
        return o;
    }

//────────────────────────────────────────────────────────────────
// getAllOrders()
//────────────────────────────────────────────────────────────────


    @Test
    @DisplayName("getOrderById: happy path")
    void getOrderById_positive() {
        // Arrange
        Order o = makeOrder("Z1");
        when(orderRepository.findOrderByOrderIdentifier_OrderId("Z1"))
                .thenReturn(o);

        // Swap first/last in stub so that enrichResponse() ends up with firstName="A", lastName="B"
        when(customerClient.getCustomerbyCustomerId("C1"))
                .thenReturn(new CustomerResponseModel("C1", "B", "A"));

        when(productClient.getCatalogById("CAT1"))
                .thenReturn(new CatalogResponseModel("CAT1", "T", "D"));

        when(productClient.getCatalogWatchByID("W1"))
                .thenReturn(WatchResponseModel.builder()
                        .catalogId("CAT1")
                        .watchId("W1")
                        .model("X")
                        .material("Y")
                        .quantity(10)
                        .usageType(UsageType.NEW)
                        .accessories(List.of())
                        .price(new com.example.orderservices.presentationlayer.productdtos.watchdtos.Price(
                                new BigDecimal("200"),
                                new BigDecimal("150"),
                                new BigDecimal("50")))
                        .watchBrand(new WatchBrand("B", "C"))
                        .build()
                );

        when(planClient.getServicePlansById("P1"))
                .thenReturn(new ServicePlanResponseModel("P1", "Cov", LocalDate.now()));

        // stub the mapper used by service.getOrderById(...)
        when(responseMapper.entityToResponseModel(o))
                .thenReturn(new OrderResponseModel(
                        "Z1",                 // orderId
                        "C1", "A", "B",       // customerFirstName="A", customerLastName="B"
                        "CAT1", "T", "D",     // catalogId, type, description
                        "W1", "X", "Y",       // watchId, model, material
                        "P1", "Cov", LocalDate.now(),
                        "O1", 200.0, "USD", "EUR",
                        LocalDateTime.now(),
                        OrderStatus.PURCHASE_COMPLETED
                ));

        // Act
        OrderResponseModel dto = service.getOrderById("Z1");

        // Assert
        assertThat(dto.getOrderId()).isEqualTo("Z1");
        assertThat(dto.getCustomerFirstName()).isEqualTo("A");
        assertThat(dto.getCustomerLastName()).isEqualTo("B");
    }
    @Test
    @DisplayName("getAllOrders: downstream client throws → order deleted & skipped")
    void getAllOrders_clientThrows() {
        Order o = makeOrder("X2");
        when(orderRepository.findAll()).thenReturn(List.of(o));
        when(customerClient.getCustomerbyCustomerId(any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        List<OrderResponseModel> all = service.getAllOrders();
        assertThat(all).isEmpty();
        verify(orderRepository).delete(o);
    }

//────────────────────────────────────────────────────────────────
// getOrderById()
//────────────────────────────────────────────────────────────────


    @Test
    @DisplayName("getOrderById: not found → throws NotFoundException")
    void getOrderById_notFound() {
        when(orderRepository.findOrderByOrderIdentifier_OrderId("NN")).thenReturn(null);
        assertThatThrownBy(() -> service.getOrderById("NN"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getOrderById: downstream null → delete + NotFoundException")
    void getOrderById_downstreamNull() {
        Order o = makeOrder("Z2");
        when(orderRepository.findOrderByOrderIdentifier_OrderId("Z2")).thenReturn(o);
        when(customerClient.getCustomerbyCustomerId("C1")).thenReturn(null);
        assertThatThrownBy(() -> service.getOrderById("Z2"))
                .isInstanceOf(NotFoundException.class);
        verify(orderRepository).delete(o);
    }

//────────────────────────────────────────────────────────────────
// createOrder()
//────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createOrder: happy path")
    void createOrder_positive() {
        LocalDateTime now = LocalDateTime.now();
        OrderRequestModel req = OrderRequestModel.builder()
                .orderName("NewOrder")
                .customerId("CC")
                .catalogId("CATX")
                .watchId("WX")
                .servicePlanId("PX")
                .salePrice(50.0)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // no existing name
        when(orderRepository.existsByOrderName("NewOrder")).thenReturn(false);

        // downstream stubs
        when(customerClient.getCustomerbyCustomerId("CC"))
                .thenReturn(new CustomerResponseModel("CC", "F", "L"));

        when(productClient.getCatalogWatchByID("WX"))
                .thenReturn(WatchResponseModel.builder()
                        .watchId("WX").catalogId("CATX").quantity(5).usageType(UsageType.NEW)
                        .model("M").material("Mat").accessories(List.of())
                        .price(new com.example.orderservices.presentationlayer.productdtos.watchdtos.Price(
                                new BigDecimal("100"), new BigDecimal("80"), new BigDecimal("20")))
                        .watchBrand(new WatchBrand("B", "C"))
                        .build());

        when(productClient.getCatalogById("CATX"))
                .thenReturn(new CatalogResponseModel("CATX", "T", "D"));

        when(planClient.getServicePlansById("PX"))
                .thenReturn(new ServicePlanResponseModel("PX", "Cov", LocalDate.now()));

        // mapper → entity
        Order built = makeOrder("R1");
        when(requestMapper.requestModelToEntity(req)).thenReturn(built);
        when(orderRepository.save(built)).thenReturn(built);

        // prepare response mapper
        OrderResponseModel resp = OrderResponseModel.builder()
                .orderId("R1")
                .customerId("CC").customerFirstName("F").customerLastName("L")
                .catalogId("CATX").catalogType("T").catalogDescription("D")
                .watchId("WX").watchModel("M").watchMaterial("Mat")
                .servicePlanId("PX").servicePlanCoverageDetails("Cov").servicePlanExpirationDate(LocalDate.now())
                .orderName("NewOrder").salePrice(50.0).saleCurrency("USD").paymentCurrency("EUR")
                .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();
        when(responseMapper.entityToResponseModel(built)).thenReturn(resp);

        // execute
        OrderResponseModel out = service.createOrder(req);

        // assertions
        assertThat(out.getOrderId()).isEqualTo("R1");

        // verify inventory update with matchers for all args
        verify(productClient).updateWatchInInventory(
                eq("CATX"),
                eq("WX"),
                any(WatchRequestModel.class)
        );
    }


@Test @DisplayName("createOrder: missing orderName → InvalidInputException")
void createOrder_missingRequired() {
    OrderRequestModel req = OrderRequestModel.builder().build();
    assertThatThrownBy(() -> service.createOrder(req))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("orderName is required");
}

@Test @DisplayName("createOrder: duplicate name → DuplicateOrderName")
void createOrder_duplicateName() {
    OrderRequestModel req = OrderRequestModel.builder()
            .orderName("dup").customerId("C").watchId("W").servicePlanId("P")
            .salePrice(1.0).currency("USD").paymentCurrency("EUR").build();
    when(orderRepository.existsByOrderName("dup")).thenReturn(true);
    assertThatThrownBy(() -> service.createOrder(req))
            .isInstanceOf(DuplicateOrderName.class);
}

@Test @DisplayName("createOrder: downstream missing → NotFoundException")
void createOrder_downstreamMissing() {
    OrderRequestModel req = OrderRequestModel.builder()
            .orderName("xx").customerId("C").watchId("W").servicePlanId("P")
            .salePrice(10.0).currency("USD").paymentCurrency("EUR").build();
    when(orderRepository.existsByOrderName("xx")).thenReturn(false);
    when(customerClient.getCustomerbyCustomerId("C")).thenReturn(null);
    assertThatThrownBy(() -> service.createOrder(req))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Customer ID 'C' not found");
}

//────────────────────────────────────────────────────────────────
// updateOrder()
//────────────────────────────────────────────────────────────────

@Test @DisplayName("updateOrder: non-existent → NotFoundException")
void updateOrder_notFound() {
    when(orderRepository.findOrderByOrderIdentifier_OrderId("nope")).thenReturn(null);
    assertThatThrownBy(() -> service.updateOrder("nope", new OrderRequestModel()))
            .isInstanceOf(NotFoundException.class);
}

    @Test
    @DisplayName("updateOrder: flip status → inventory update")
    void updateOrder_positive() {
        // existing order in PURCHASE_COMPLETED state
        Order existing = makeOrder("U1");
        existing.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);
        when(orderRepository.findOrderByOrderIdentifier_OrderId("U1"))
                .thenReturn(existing);

        // build a request that flips it to CANCELED
        OrderRequestModel req = OrderRequestModel.builder()
                .orderName("O1")
                .orderStatus(OrderStatus.PURCHASE_CANCELED)
                .currency("USD")
                .paymentCurrency("EUR")
                .salePrice(100.0)
                .build();

        // saving returns the same entity
        when(orderRepository.save(existing)).thenReturn(existing);

        // downstream stubs needed by enrichResponse():
        when(customerClient.getCustomerbyCustomerId("C1"))
                .thenReturn(new CustomerResponseModel("C1", "F", "L"));
        when(productClient.getCatalogById("CAT1"))
                .thenReturn(new CatalogResponseModel("CAT1", "T", "D"));
        when(planClient.getServicePlansById("P1"))
                .thenReturn(new ServicePlanResponseModel("P1", "Cov", LocalDate.now()));

        // inventory lookup for update logic
        WatchResponseModel watch = WatchResponseModel.builder()
                .watchId("W1").catalogId("CAT1").quantity(2)
                .usageType(UsageType.NEW).model("M").material("Mat")
                .accessories(List.of())
                .price(new com.example.orderservices.presentationlayer.productdtos.watchdtos.Price(
                        new BigDecimal("100"),
                        new BigDecimal("80"),
                        new BigDecimal("20")
                ))
                .watchBrand(new WatchBrand("B", "C"))
                .build();
        when(productClient.getCatalogWatchByID("W1")).thenReturn(watch);

        // stub the final mapper
        when(responseMapper.entityToResponseModel(existing))
                .thenReturn(new OrderResponseModel(
                        "U1", "C1", "F", "L",
                        "CAT1", "T", "D",
                        "W1", "M", "Mat",
                        "P1", "Cov", LocalDate.now(),
                        "O1", 100.0, "USD", "EUR",
                        LocalDateTime.now(),
                        OrderStatus.PURCHASE_CANCELED
                ));

        // call under test
        OrderResponseModel out = service.updateOrder("U1", req);

        // assert new status
        assertThat(out.getOrderStatus())
                .isEqualTo(OrderStatus.PURCHASE_CANCELED);

        // verify we called updateWatchInInventory once, matcher’ed for all params
        verify(productClient).updateWatchInInventory(
                eq("CAT1"),
                eq("W1"),
                any(WatchRequestModel.class)
        );
    }

//────────────────────────────────────────────────────────────────
// deleteOrder()
//────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteOrder: completed → restore stock & message")
    void deleteOrder_positive() {
        Order o = makeOrder("D1");
        o.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);
        when(orderRepository.findOrderByOrderIdentifier_OrderId("D1"))
                .thenReturn(o);

        WatchResponseModel watch = WatchResponseModel.builder()
                .watchId("W1").catalogId("CAT1").quantity(3)
                .usageType(UsageType.NEW).model("M").material("Mat")
                .accessories(List.of())
                .price(new com.example.orderservices.presentationlayer.productdtos.watchdtos.Price(
                        new BigDecimal("100"),
                        new BigDecimal("80"),
                        new BigDecimal("20")))
                .watchBrand(new WatchBrand("B","C"))
                .build();
        when(productClient.getCatalogWatchByID("W1")).thenReturn(watch);

        String msg = service.deleteOrder("D1");

        // match the actual phrasing:
        assertThat(msg)
                .contains("Order 'D1' deleted")
                .contains("stock restored");

        verify(productClient).updateWatchInInventory(
                eq("CAT1"), eq("W1"), any(WatchRequestModel.class)
        );
        verify(orderRepository).delete(o);
    }


@Test
@DisplayName("deleteOrder: missing → NotFoundException")
void deleteOrder_notFound() {
    when(orderRepository.findOrderByOrderIdentifier_OrderId("none")).thenReturn(null);
    assertThatThrownBy(() -> service.deleteOrder("none"))
            .isInstanceOf(NotFoundException.class);
}
}
