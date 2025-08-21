package com.example.orderservices.presentationlayer;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.within;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.orderservices.businesslogiclayer.OrderService;
import com.example.orderservices.dataaccesslayer.OrderStatus;
import com.example.orderservices.domainclientlayer.CustomerServiceClient;
import com.example.orderservices.domainclientlayer.ProductServiceClient;
import com.example.orderservices.domainclientlayer.ServicePlanServiceClient;
import com.example.orderservices.presentationlayer.customerdtos.CustomerResponseModel;

import com.example.orderservices.presentationlayer.productdtos.catalogdtos.CatalogResponseModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.*;
import com.example.orderservices.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.example.orderservices.utils.DuplicateOrderName;
import com.example.orderservices.utils.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.data.mongodb.port=0",                     // random free port
        "spring.mongodb.embedded.version=5.0.5",           // the Spring Boot 2 style
        "de.flapdoodle.mongodb.embedded.version=5.0.5"    // the de.flapdoodle override
})
@AutoConfigureWebTestClient
public class OrderControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;


    @MockBean
    private CustomerServiceClient customerServiceClient;

    @MockBean
    private ProductServiceClient productServiceClient;

    @MockBean
    private ServicePlanServiceClient servicePlanServiceClient;

    private void stubDownstream(String customerId,
                                String catalogId,
                                String watchId,
                                String planId,
                                LocalDateTime now) {

        // --- Customer stub with phone number ---
        given(customerServiceClient.getCustomerbyCustomerId(customerId))
                .willReturn(CustomerResponseModel.builder()
                        .customerId(customerId)
                        .firstName("Alice")
                        .lastName("Smith")
                        // .emailAddress(...)   ← remove
                        // .phoneNumbers(...)    ← remove
                        .build());

        // --- Catalog stub ---
        given(productServiceClient.getCatalogById(catalogId))
                .willReturn(CatalogResponseModel.builder()
                        .catalogId(catalogId)
                        .type("Luxury")
                        .description("High-end watches")
                        .build());

        // --- Watch stub with accessories, price, brand & usage ---
        given(productServiceClient.getCatalogWatchByID(watchId))
                .willReturn(WatchResponseModel.builder()
                        .watchId(watchId)
                        .catalogId(catalogId)
                        .model("Chronograph")
                        .material("Steel")
                        .quantity(1)
//                        .accessories(List.of(
//                                Accessory.builder()
//                                        .accessoryName("Leather Strap")
//                                        .accessoryCost(new BigDecimal("49.99"))
//                                        .build(),
//                                Accessory.builder()
//                                        .accessoryName("Travel Case")
//                                        .accessoryCost(new BigDecimal("29.99"))
//                                        .build()
//                        ))
//                        .price(Price.builder()
//                                .msrp(new BigDecimal("499.95"))
//                                .cost(new BigDecimal("450.00"))
//                                .totalOptionsCost(new BigDecimal("79.98"))
//                                .build())
//                        .watchBrand(WatchBrand.builder()
//                                .brandName("Rolex")
//                                .brandCountry("Switzerland")
//                                .build())
//                        .usageType(UsageType.NEW)
                        .build());

        // --- ServicePlan stub with LocalDate expiration ---
        given(servicePlanServiceClient.getServicePlansById(planId))
                .willReturn(ServicePlanResponseModel.builder()
                        .planId(planId)
                        .coverageDetails("2-year full coverage")
                        .expirationDate(now.toLocalDate().plusYears(2))
                        .build());
    }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    @DisplayName("GET /api/v1/orders → 200 + empty list when no orders in repo")
    void getAllOrders_whenNoneExist_thenEmpty() {
        webTestClient.get()
                .uri("/api/v1/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseModel.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET /api/v1/orders → 404 for wrong path")
    void getAllOrders_negative() {
        webTestClient.get()
                .uri("/api/v1/order")   // typo on purpose
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/v1/orders → 201 when valid")
    void createOrder_positive() {

        String customerId = "C123", catalogId = "CAT9", watchId = "W42", planId = "P7";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        stubDownstream(customerId, catalogId, watchId, planId, now);

        OrderRequestModel req = OrderRequestModel.builder()
                .customerId(customerId)
                .catalogId(catalogId)
                .watchId(watchId)
                .servicePlanId(planId)
                .orderName("Sale")
                .salePrice(100.0)
                .currency("EUR")
                .paymentCurrency("USD")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .value(resp -> {
                    // ─── Order basics ───
                    assertThat(resp.getOrderId()).isNotBlank();
                    assertThat(resp.getOrderName()).isEqualTo("Sale");
                    assertThat(resp.getSalePrice()).isEqualTo(100.0);
                    assertThat(resp.getOrderDate()).isCloseTo(now, within(1, ChronoUnit.SECONDS));
                    assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);

                    // ─── Customer info ───
                    assertThat(resp.getCustomerFirstName()).isEqualTo("Alice");
                    assertThat(resp.getCustomerLastName()).isEqualTo("Smith");
//                        assertThat(resp.getCustomerEmailAddress()).isEqualTo("alice@example.com");
//                        assertThat(resp.getCustomerPhoneNumbers())
//                                .hasSize(1)
//                                .extracting(PhoneNumber::getNumber, PhoneNumber::getType)
//                                .containsExactly(tuple("555-1234", PhoneType.MOBILE));

                    // ─── Catalog info ───
                    assertThat(resp.getCatalogType()).isEqualTo("Luxury");
                    assertThat(resp.getCatalogDescription()).isEqualTo("High-end watches");

                    // ─── Watch info ───
                    assertThat(resp.getWatchModel()).isEqualTo("Chronograph");
                    assertThat(resp.getWatchMaterial()).isEqualTo("Steel");
//                        assertThat(resp.getWatchQuantity()).isEqualTo(1);

                    // Accessories
//                        assertThat(resp.getWatchAccessories())
//                                .hasSize(2)
//                                .extracting(Accessory::getAccessoryName, Accessory::getAccessoryCost)
//                                .containsExactlyInAnyOrder(
//                                        tuple("Leather Strap", new BigDecimal("49.99")),
//                                        tuple("Travel Case",   new BigDecimal("29.99"))
//                                );

//                        // Price details
//                        Price price = resp.getWatchPrice();
//                        assertThat(price.getMsrp()).isEqualByComparingTo("499.95");
//                        assertThat(price.getCost()).isEqualByComparingTo("450.00");
//                        assertThat(price.getTotalOptionsCost()).isEqualByComparingTo("79.98");
//
//                        // Brand & usage
//                        assertThat(resp.getWatchBrand().getBrandName()).isEqualTo("Rolex");
//                        assertThat(resp.getWatchBrand().getBrandCountry()).isEqualTo("Switzerland");
//                        assertThat(resp.getUsageType()).isEqualTo(UsageType.NEW);

                    // Service plan
                    assertThat(resp.getServicePlanCoverageDetails()).isEqualTo("2-year full coverage");
                    assertThat(resp.getServicePlanExpirationDate())
                            .isEqualTo(now.toLocalDate().plusYears(2));
                });
    }

    @Test
    @DisplayName("POST /api/v1/orders → 422 when missing required field")
    void createOrder_negative() {
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ \"orderName\": \"Oops\" }")
                .exchange()
                // was .expectStatus().isBadRequest();
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ---------------------
    // GET BY ID
    // ---------------------

    @Test
    @DisplayName("GET /api/v1/orders/{id} → 200 when exists")
    void getById_positive() {
        // first create one
        String customerId = "C123", catalogId = "CAT9", watchId = "W42", planId = "P7";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        stubDownstream(customerId, catalogId, watchId, planId, now);

        OrderResponseModel created = webTestClient.post()
                .uri("/api/v1/orders")
                .bodyValue(OrderRequestModel.builder()
                        .customerId(customerId)
                        .catalogId(catalogId)
                        .watchId(watchId)
                        .servicePlanId(planId)
                        .orderName("FetchMe")
                        .salePrice(42.0)
                        .currency("EUR")
                        .paymentCurrency("USD")
                        .orderDate(now)
                        .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri("/api/v1/orders/{id}", created.getOrderId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(r -> assertThat(r.getOrderName()).isEqualTo("FetchMe"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} → 404 when not found")
    void getById_negative() {
        webTestClient.get()
                .uri("/api/v1/orders/{id}", UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isNotFound();
    }

    // ---------------------
    // PUT
    // ---------------------

    @Test
    @DisplayName("PUT /api/v1/orders/{id} → 200 when exists (name immutable)")
    void updateOrder_positive() {
        // create one
        String customerId = "C123", catalogId = "CAT9", watchId = "W42", planId = "P7";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        stubDownstream(customerId, catalogId, watchId, planId, now);

        OrderResponseModel created = webTestClient.post()
                .uri("/api/v1/orders")
                .bodyValue(OrderRequestModel.builder()
                        .customerId(customerId)
                        .catalogId(catalogId)
                        .watchId(watchId)
                        .servicePlanId(planId)
                        .orderName("Old")              // original name
                        .salePrice(10.0)
                        .currency("EUR")
                        .paymentCurrency("USD")
                        .orderDate(now)
                        .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .returnResult().getResponseBody();

        // now update everything except orderName
        webTestClient.put()
                .uri("/api/v1/orders/{id}", created.getOrderId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderRequestModel.builder()
                        .customerId(customerId)
                        .catalogId(catalogId)
                        .watchId(watchId)
                        .servicePlanId(planId)
                        .orderName("Old")              // must remain the same
                        .salePrice(99.99)              // updated
                        .currency("EUR")
                        .paymentCurrency("USD")
                        .orderDate(now.plusDays(1))    // updated
                        .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(r -> {
                    assertThat(r.getOrderName()).isEqualTo("Old");        // still Old
                    assertThat(r.getSalePrice()).isEqualTo(99.99);        // new price
                    assertThat(r.getOrderDate()).isEqualTo(now.plusDays(1));
                    assertThat(r.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);
                });
    }

    @Test
    @DisplayName("PUT /api/v1/orders/{id} → 404 when missing")
    void updateOrder_negative() {
        webTestClient.put()
                .uri("/api/v1/orders/{id}", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(OrderRequestModel.builder().build())
                .exchange()
                .expectStatus().isNotFound();
    }

    // ---------------------
    // DELETE
    // ---------------------

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} → 200 + then gone")
    void deleteOrder_positive() {
        // create one
        String customerId = "C123", catalogId = "CAT9", watchId = "W42", planId = "P7";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        stubDownstream(customerId, catalogId, watchId, planId, now);

        OrderResponseModel created = webTestClient.post()
                .uri("/api/v1/orders")
                .bodyValue(OrderRequestModel.builder()
                        .customerId(customerId)
                        .catalogId(catalogId)
                        .watchId(watchId)
                        .servicePlanId(planId)
                        .orderName("DeleteMe")
                        .salePrice(5.0)
                        .currency("EUR")
                        .paymentCurrency("USD")
                        .orderDate(now)
                        .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .returnResult().getResponseBody();

        webTestClient.delete()
                .uri("/api/v1/orders/{id}", created.getOrderId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(msg -> assertThat(msg).contains("deleted successfully"));

        // verify gone
        webTestClient.get()
                .uri("/api/v1/orders/{id}", created.getOrderId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} → 404 when not found")
    void deleteOrder_negative() {
        webTestClient.delete()
                .uri("/api/v1/orders/{id}", UUID.randomUUID().toString())
                .exchange()
                .expectStatus().isNotFound();
    }


    // ─── Negative Exception Path: duplicate order name → 422 UNPROCESSABLE_ENTITY ───
    @Test
    @DisplayName("POST /api/v1/orders → 422 UNPROCESSABLE_ENTITY when order name already exists")
    void createOrder_duplicateName_returnsUnprocessableEntity() {
        String customerId = "C123", catalogId = "CAT9", watchId = "W42", planId = "P7";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        stubDownstream(customerId, catalogId, watchId, planId, now);

        String dupName = "DUPLICATE-" + UUID.randomUUID();
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId(customerId)
                .catalogId(catalogId)
                .watchId(watchId)
                .servicePlanId(planId)
                .orderName(dupName)
                .salePrice(100.0)
                .currency("EUR")
                .paymentCurrency("USD")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // first insert succeeds
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();

        // stub again for the second call
        stubDownstream(customerId, catalogId, watchId, planId, now);

        // second insert triggers DuplicateOrderName → 422
        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(err -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, err.getHttpStatus());
                    assertTrue(err.getMessage().contains("order with order name " + dupName + " already exists."));
                });
    }


    @Test
    @DisplayName("POST /api/v1/orders → 201 CREATED when order name is new")
    void createOrder_uniqueName_returnsCreated() {
        String customerId = "C123", catalogId = "CAT9", watchId = "W42", planId = "P7";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        stubDownstream(customerId, catalogId, watchId, planId, now);

        String newName = "ORDER-" + UUID.randomUUID();
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId(customerId)
                .catalogId(catalogId)
                .watchId(watchId)
                .servicePlanId(planId)
                .orderName(newName)
                .salePrice(100.0)
                .currency("EUR")
                .paymentCurrency("USD")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        webTestClient.post()
                .uri("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .value(resp -> {
                    assertThat(resp.getOrderId()).isNotBlank();
                    assertThat(resp.getOrderName()).isEqualTo(newName);
                });
    }


    @Test
    void builders_getters_setters_and_json_roundtrip() throws Exception {
        // 1) build every nested DTO

        CustomerResponseModel cust = CustomerResponseModel.builder()
                .customerId("C1")
                .firstName("Bob")
                .lastName("Jones")
                .build();
        assertThat(cust.getFirstName()).isEqualTo("Bob");
        assertThat(cust.getLastName()).isEqualTo("Jones");

        CatalogResponseModel catalog = CatalogResponseModel.builder()
                .catalogId("CAT1")
                .type("Standard")
                .description("Everyday watches")
                .build();
        assertThat(catalog.getCatalogId()).isEqualTo("CAT1");
        assertThat(catalog.getType()).isEqualTo("Standard");
        assertThat(catalog.getDescription()).isEqualTo("Everyday watches");

        Accessory acc1 = Accessory.builder()
                .accessoryName("Band")
                .accessoryCost(new BigDecimal("19.99"))
                .build();
        Accessory acc2 = Accessory.builder()
                .accessoryName("Box")
                .accessoryCost(new BigDecimal("9.99"))
                .build();
        assertThat(acc1.getAccessoryName()).isEqualTo("Band");
        assertThat(acc1.getAccessoryCost()).isEqualByComparingTo("19.99");
        assertThat(acc1).isNotEqualTo(acc2);

        Price price = Price.builder()
                .msrp(new BigDecimal("299.95"))
                .cost(new BigDecimal("250.00"))
                .totalOptionsCost(new BigDecimal("29.98"))
                .build();
        assertThat(price.getMsrp()).isEqualByComparingTo("299.95");
        assertThat(price.getCost()).isEqualByComparingTo("250.00");
        assertThat(price.getTotalOptionsCost()).isEqualByComparingTo("29.98");

        WatchBrand brand = WatchBrand.builder()
                .brandName("Omega")
                .brandCountry("Switzerland")
                .build();
        assertThat(brand.getBrandName()).isEqualTo("Omega");
        assertThat(brand.getBrandCountry()).isEqualTo("Switzerland");
        assertThat(brand.toString()).contains("Omega");

        WatchResponseModel watch = WatchResponseModel.builder()
                .watchId("W1")
                .catalogId(catalog.getCatalogId())
                .model("Seamaster")
                .material("Steel")
                .quantity(2)
                .accessories(List.of(acc1, acc2))
                .price(price)
                .watchBrand(brand)
                .usageType(UsageType.NEW)
                .build();
        // exercise all getters:
        assertThat(watch.getWatchId()).isEqualTo("W1");
        assertThat(watch.getCatalogId()).isEqualTo("CAT1");
        assertThat(watch.getModel()).isEqualTo("Seamaster");
        assertThat(watch.getMaterial()).isEqualTo("Steel");
        assertThat(watch.getQuantity()).isEqualTo(2);
        assertThat(watch.getAccessories()).hasSize(2).containsExactly(acc1, acc2);
        assertThat(watch.getPrice()).isEqualTo(price);
        assertThat(watch.getWatchBrand()).isEqualTo(brand);
        assertThat(watch.getUsageType()).isEqualTo(UsageType.NEW);

        ServicePlanResponseModel plan = ServicePlanResponseModel.builder()
                .planId("P1")
                .coverageDetails("3-year coverage")
                .expirationDate(LocalDate.now().plusYears(3))
                .build();
        assertThat(plan.getPlanId()).isEqualTo("P1");
        assertThat(plan.getCoverageDetails()).isEqualTo("3-year coverage");
        assertThat(plan.getExpirationDate().getYear())
                .isEqualTo(LocalDate.now().plusYears(3).getYear());

        // 2) build the top–level request & response
        LocalDateTime now = LocalDateTime.now().withNano(0);
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId(cust.getCustomerId())
                .catalogId(catalog.getCatalogId())
                .watchId(watch.getWatchId())
                .servicePlanId(plan.getPlanId())
                .orderName("Order1")
                .salePrice(123.45)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();
        assertThat(req.getCustomerId()).isEqualTo("C1");
        assertThat(req.getOrderName()).isEqualTo("Order1");

        OrderResponseModel resp = new OrderResponseModel(
                "O1",
                cust.getCustomerId(),
                cust.getFirstName(),
                cust.getLastName(),
                catalog.getCatalogId(),
                catalog.getType(),
                catalog.getDescription(),
                watch.getWatchId(),
                watch.getModel(),
                watch.getMaterial(),
                plan.getPlanId(),
                plan.getCoverageDetails(),
                plan.getExpirationDate(),
                req.getOrderName(),
                req.getSalePrice(),
                req.getCurrency(),
                req.getPaymentCurrency(),
                req.getOrderDate(),
                req.getOrderStatus()
        );
        assertThat(resp.getOrderId()).isEqualTo("O1");
        assertThat(resp.getCustomerFirstName()).isEqualTo("Bob");
        assertThat(resp.getCustomerLastName()).isEqualTo("Jones");
        assertThat(resp.getCatalogDescription()).isEqualTo("Everyday watches");
        assertThat(resp.getServicePlanExpirationDate()).isEqualTo(plan.getExpirationDate());

        // 3) JSON serialize / deserialize each DTO
        String json = MAPPER.writeValueAsString(resp);
        OrderResponseModel roundTrip = MAPPER.readValue(json, OrderResponseModel.class);
        assertThat(roundTrip).isEqualTo(resp);

        // JSON round-trip for WatchResponseModel with full coverage:
        String wJson = MAPPER.writeValueAsString(watch);
        WatchResponseModel wRt = MAPPER.readValue(wJson, WatchResponseModel.class);
        assertThat(wRt.getWatchId()).isEqualTo(watch.getWatchId());
        assertThat(wRt.getCatalogId()).isEqualTo(watch.getCatalogId());
        assertThat(wRt.getModel()).isEqualTo(watch.getModel());
        assertThat(wRt.getMaterial()).isEqualTo(watch.getMaterial());
        assertThat(wRt.getQuantity()).isEqualTo(watch.getQuantity());
        assertThat(wRt.getAccessories()).hasSize(2).containsExactly(acc1, acc2);
        assertThat(wRt.getPrice()).isEqualTo(price);
        assertThat(wRt.getWatchBrand()).isEqualTo(brand);
        assertThat(wRt.getUsageType()).isEqualTo(UsageType.NEW);

        // and Price
        Price pRt = MAPPER.readValue(MAPPER.writeValueAsString(price), Price.class);
        assertThat(pRt).isEqualTo(price);

        // and enum
        assertThat(UsageType.valueOf("NEW")).isEqualTo(UsageType.NEW);
    }

}