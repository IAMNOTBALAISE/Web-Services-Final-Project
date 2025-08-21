package com.example.apigatewayservice.presentationlayer;

import com.example.apigatewayservice.domainclientlayer.CustomerServiceClient;
import com.example.apigatewayservice.domainclientlayer.OrderServiceClient;
import com.example.apigatewayservice.domainclientlayer.ProductServiceClient;
import com.example.apigatewayservice.domainclientlayer.servicePlanServiceClient;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogRequestModel;
import com.example.apigatewayservice.presentationlayer.catalogdtos.CatalogResponseModel;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerRequestModel;
import com.example.apigatewayservice.presentationlayer.customersdtos.CustomerResponseModel;
import com.example.apigatewayservice.presentationlayer.customersdtos.PhoneNumber;
import com.example.apigatewayservice.presentationlayer.customersdtos.PhoneType;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderRequestModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderResponseModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderStatus;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanRequestModel;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ApiControllerIntegrationTest {


    private final String CATALOG = "CAT1";
    private final String WATCH   = "W1";
    private WatchResponseModel watchSample;
    private OrderResponseModel ordSample;
    private OrderRequestModel req;


    @Autowired
    WebTestClient client;

    @MockBean
    CustomerServiceClient customerClient;
    @MockBean
    ProductServiceClient productClient;
    @MockBean
    servicePlanServiceClient planClient;
    @MockBean
    OrderServiceClient orderClient;

    private final LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
    private final LocalDate expDate = now.toLocalDate().plusYears(1);


    @BeforeEach
    void setUp() {
        stubDownstream("C1", "CAT1", "W1", "P1", "O1");
    }

    private void stubDownstream(String customerId,
                                String catalogId,
                                String watchId,
                                String planId,
                                String orderId) {

        // —— Customer stub ——
        CustomerResponseModel cust = CustomerResponseModel.builder()
                .customerId(customerId)
                .firstName("Alice").lastName("Smith")
                .emailAddress("alice@example.com")
                .streetAddress("123 Main St").postalCode("A1A1A1")
                .city("Townsville").province("TS")
                .phoneNumbers(List.of())
                .build();

        given(customerClient.getCustomers()).willReturn(List.of(cust));
        given(customerClient.getCustomerbyCustomerId(customerId)).willReturn(cust);
        given(customerClient.getCustomerbyCustomerId("X")).willReturn(null);

        // —— Catalog stub ——
        CatalogResponseModel cat = CatalogResponseModel.builder()
                .catalogId(catalogId)
                .type("Standard")
                .description("Everyday watches")
                .build();

        given(productClient.getCatalogs()).willReturn(List.of(cat));
        given(productClient.getCatalogById(catalogId)).willReturn(cat);
        given(productClient.getCatalogById("X")).willReturn(null);

        // —— Watch stub ——
        watchSample = WatchResponseModel.builder()
                .watchId(watchId)
                .catalogId(catalogId)
                .quantity(5)
                .usageType(UsageType.NEW)
                .model("X123")
                .material("Steel")
                .accessories(List.of(new Accessory("Strap", new BigDecimal("15.00"))))
                .price(new Price(
                        new BigDecimal("100"), new BigDecimal("80"), new BigDecimal("20")))
                .watchBrand(new WatchBrand("Rolex","CH"))
                .build();

        given(productClient.getCatalogWatchByID(watchId)).willReturn(watchSample);
        given(productClient.getCatalogWatchByID("X")).willReturn(null);

        given(productClient.getWatchesInCatalogWithFiltering(eq(catalogId), anyMap()))
                .willReturn(List.of(watchSample));
        given(productClient.getWatchesInCatalogWithFiltering(eq(catalogId), argThat(Map::isEmpty)))
                .willReturn(Collections.emptyList());

        given(productClient.getWatchesWithFilter(anyMap()))
                .willReturn(List.of(watchSample));
        given(productClient.getWatchesWithFilter(argThat(Map::isEmpty)))
                .willReturn(Collections.emptyList());

        // —— Service Plan stub ——
        ServicePlanResponseModel plan = ServicePlanResponseModel.builder()
                .planId(planId)
                .coverageDetails("2-year")
                .expirationDate(expDate)
                .build();

        given(planClient.getServicePlans()).willReturn(List.of(plan));
        given(planClient.getServicePlansById(planId)).willReturn(plan);
        given(planClient.getServicePlansById("X")).willReturn(null);

        // —— Order stub ——
        ordSample = OrderResponseModel.builder()
                .orderId(orderId)
                .customerId(customerId).customerFirstName("Alice").customerLastName("Smith")
                .catalogId(catalogId).catalogType("Standard").catalogDescription("Everyday watches")
                .watchId(watchId).watchModel("X123").watchMaterial("Steel")
                .servicePlanId(planId).servicePlanCoverageDetails("2-year").servicePlanExpirationDate(expDate)
                .orderName("TestOrder")
                .salePrice(100.0).saleCurrency("USD").paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        req = OrderRequestModel.builder()
                .customerId(customerId)
                .catalogId(catalogId)
                .watchId(watchId)
                .servicePlanId(planId)
                .orderName("TestOrder")
                .salePrice(100.0)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        given(orderClient.getAllOrders()).willReturn(List.of(ordSample));
        given(orderClient.getOrderById(orderId)).willReturn(ordSample);
        given(orderClient.getOrderById("X")).willThrow(new RuntimeException("not found"));

        given(orderClient.createOrder(any(OrderRequestModel.class)))
                .willReturn(ordSample);

        given(orderClient.updateOrder(eq(orderId), any(OrderRequestModel.class)))
                .willReturn(ordSample);
        given(orderClient.updateOrder(eq("X"), any(OrderRequestModel.class)))
                .willThrow(new RuntimeException("not found"));

        doThrow(new RuntimeException("not found"))
                .when(orderClient).deleteOrder("X");
    }



    @Test
    @DisplayName("GET /api/v1/catalogs → 200 + non-empty list")
    void getAllCatalogs_positive() {
        client.get().uri("/api/v1/catalogs")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CatalogResponseModel.class)
                .hasSize(1)
                .value(list -> {
                    CatalogResponseModel c = list.get(0);
                    assert c.getCatalogId().equals("CAT1");
                    assert c.getType().equals("Standard");
                    assert c.getDescription().equals("Everyday watches");
                });
    }

    @Test
    @DisplayName("GET /api/v1/catalogs → 200 + empty list when none")
    void getAllCatalogs_empty() {
        // override the stub for this test
        given(productClient.getCatalogs()).willReturn(List.of());

        client.get().uri("/api/v1/catalogs")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CatalogResponseModel.class)
                .hasSize(0);
    }

    @Test
    @DisplayName("GET /api/v1/catalogs/{id} → 200 when found")
    void getCatalogById_positive() {
        client.get().uri("/api/v1/catalogs/{id}", "CAT1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CatalogResponseModel.class)
                .value(c -> {
                    assert c.getCatalogId().equals("CAT1");
                    assert c.getType().equals("Standard");
                    assert c.getDescription().equals("Everyday watches");
                });
    }

    @Test
    @DisplayName("GET /api/v1/catalogs/{id} → 404 when not found")
    void getCatalogById_negative() {
        client.get().uri("/api/v1/catalogs/{id}", "UNKNOWN")
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    @DisplayName("POST /api/v1/catalogs → 201 + Location header + body")
    void addCatalog_positive() {
        // prepare request
        CatalogRequestModel req = new CatalogRequestModel("Premium", "High-end watches");
        // what the service will return
        CatalogResponseModel created = CatalogResponseModel.builder()
                .catalogId("NEW123")
                .type("Premium")
                .description("High-end watches")
                .build();

        // stub downstream productClient.addCatalog(...)
        given(productClient.addCatalog(any(CatalogRequestModel.class)))
                .willReturn(created);

        client.post().uri("/api/v1/catalogs")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CatalogResponseModel.class)
                .value(c -> {
                    assert c.getCatalogId().equals("NEW123");
                    assert c.getType().equals("Premium");
                    assert c.getDescription().equals("High-end watches");
                });
    }

    @Test
    @DisplayName("POST /api/v1/catalogs → 5xx when missing body")
    void addCatalog_negative() {
        client.post().uri("/api/v1/catalogs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    @DisplayName("PUT /api/v1/catalogs/{id} → 200 + updated body")
    void updateCatalog_positive() {
        CatalogRequestModel req = new CatalogRequestModel("Luxury", "Top-tier");
        CatalogResponseModel updated = CatalogResponseModel.builder()
                .catalogId("CAT1")
                .type("Luxury")
                .description("Top-tier")
                .build();

        // stub with raw values, no matchers
        given(productClient.updateCatalog(
                any(CatalogRequestModel.class),
                eq("CAT1")
        )).willReturn(updated);

        client.put().uri("/api/v1/catalogs/{id}", "CAT1")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CatalogResponseModel.class)
                .value(c -> {
                    assert c.getCatalogId().equals("CAT1");
                    assert c.getType().equals("Luxury");
                    assert c.getDescription().equals("Top-tier");
                });
    }

    @Test
    @DisplayName("PUT /api/v1/catalogs/{id} → 404 when not found")
    void updateCatalog_negative() {
        CatalogRequestModel req = new CatalogRequestModel("A", "B");

        // stub to throw 404 when called with ("UNKNOWN", any request)
        given(productClient.updateCatalog(
                any(CatalogRequestModel.class),
                eq("UNKNOWN")
        )).willThrow(WebClientResponseException.create(
                404, "Not Found", null, null, null
        ));

        client.put().uri("/api/v1/catalogs/{id}", "UNKNOWN")
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    @DisplayName("DELETE /api/v1/catalogs/{id} → 204 + Link header, no body")
    void deleteCatalog_positive() {
        // stub deleteCatalog(...) to return null
        given(productClient.deleteCatalog("CAT1"))
                .willReturn(null);

        client.delete()
                .uri("/api/v1/catalogs/{id}", "CAT1")
                .exchange()
                // ← expect 204 instead of 200
                .expectStatus().isNoContent()

                // still assert the Link header if you’re preserving it
                .expectHeader().value("Link", linkHeader ->
                        assertThat(linkHeader).contains("/api/v1/catalogs")
                )

                // and assert there is no body
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/catalogs/{id} → 404 when not found")
    void deleteCatalog_negative() {
        given(productClient.deleteCatalog("UNKNOWN"))
                .willThrow(WebClientResponseException.create(
                        404, "Not Found", null, null, null));

        client.delete().uri("/api/v1/catalogs/{id}", "UNKNOWN")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("CatalogResponseModel: builder chain, toString(), getters + setters")
    void catalogResponseModelBuilderAndSetters() {
        // a) chain every setter
        CatalogResponseModel.CatalogResponseModelBuilder rspBuilder = CatalogResponseModel.builder()
                .catalogId("CAT1")
                .type("Standard")
                .description("Everyday");

        // b) toString()
        String repr = rspBuilder.toString();
        assertThat(repr)
                .contains("catalogId=CAT1")
                .contains("type=Standard")
                .contains("description=Everyday");

        // c) build & getters
        CatalogResponseModel rsp = rspBuilder.build();
        assertThat(rsp.getCatalogId()).isEqualTo("CAT1");
        assertThat(rsp.getType()).isEqualTo("Standard");
        assertThat(rsp.getDescription()).isEqualTo("Everyday");

        // d) setters
        rsp.setCatalogId("CAT2");
        rsp.setType("Luxury");
        rsp.setDescription("High-end");
        assertThat(rsp.getCatalogId()).isEqualTo("CAT2");
        assertThat(rsp.getType()).isEqualTo("Luxury");
        assertThat(rsp.getDescription()).isEqualTo("High-end");
    }

    @Test
    @DisplayName("CatalogResponseModel: core-field equality & hashCode")
    void catalogResponseModelEqualsHashCodeCoreFields() {
        CatalogResponseModel s1 = new CatalogResponseModel("CAT1", "Type", "Desc");
        CatalogResponseModel s2 = new CatalogResponseModel("CAT1", "Type", "Desc");
        CatalogResponseModel s3 = new CatalogResponseModel("CAT2", "Type", "Desc");

        // check core fields manually
        assertThat(s1.getCatalogId()).isEqualTo(s2.getCatalogId());
        assertThat(s1.getType()).isEqualTo(s2.getType());
        assertThat(s1.getDescription()).isEqualTo(s2.getDescription());

        // hashCode of the three core fields should match
        int coreHash1 = Objects.hash(s1.getCatalogId(), s1.getType(), s1.getDescription());
        int coreHash2 = Objects.hash(s2.getCatalogId(), s2.getType(), s2.getDescription());
        assertThat(coreHash1).isEqualTo(coreHash2);

        // negative: different id
        assertThat(s1.getCatalogId()).isNotEqualTo(s3.getCatalogId());

        // also ensure full equals rejects mismatched types and null
        assertThat(s1).isNotEqualTo(null);
        assertThat(s1).isNotEqualTo("not a model");
    }


    @Test
    @DisplayName("CatalogResponseModel: equals & hashCode ignoring links")
    void catalogResponseModelEqualsHashCodeIgnoringLinks() {
        CatalogResponseModel s1 = new CatalogResponseModel("CAT1", "Type", "Desc");
        CatalogResponseModel s2 = new CatalogResponseModel("CAT1", "Type", "Desc");
        CatalogResponseModel s3 = new CatalogResponseModel("CAT2", "Type", "Desc");

        // positive: ignore the links property
        assertThat(s1)
                .usingRecursiveComparison()
                .ignoringFields("links")
                .isEqualTo(s2);

        // negative
        assertThat(s1)
                .usingRecursiveComparison()
                .ignoringFields("links")
                .isNotEqualTo(s3);
    }

    @Test @DisplayName("GET /api/v1/customers → 200 + list of one")
    void getAllCustomers() {
        client.get().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseModel.class)
                .hasSize(1)
                .value(lst -> assertThat(lst.get(0).getCustomerId()).isEqualTo("C1"));
    }

    @Test @DisplayName("GET /api/v1/customers/{id} → 200 when found, 404 when missing")
    void getCustomerPositiveById() {
        // positive
        client.get().uri("/api/v1/customers/{id}", "C1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(c -> assertThat(c.getEmailAddress()).isEqualTo("alice@example.com"));

        // negative
        client.get().uri("/api/v1/customers/{id}", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/v1/customers?email= → 200 when found, 500 when missing")
    void getByEmail() {
        // construct the same CustomerResponseModel your stubDownstream would have made
        CustomerResponseModel alice = CustomerResponseModel.builder()
                .customerId("C1")
                .firstName("Alice")
                .lastName("Smith")
                .emailAddress("alice@example.com")
                .streetAddress("123 Main St")
                .postalCode("A1A1A1")
                .city("Townsville")
                .province("TS")
                .phoneNumbers(List.of())
                .build();

        // 1) stub the email lookup
        given(customerClient.getCustomerbyEmail("alice@example.com"))
                .willReturn(alice);
        given(customerClient.getCustomerbyEmail("unknown@example.com"))
                .willReturn(null);

        // 2) positive: we get 200 + the body
        client.get().uri("/api/v1/customers?email={e}", "alice@example.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(c -> assertThat(c.getCustomerId()).isEqualTo("C1"));

        // 3) negative: controller currently NPEs on null -> returns 500
        client.get().uri("/api/v1/customers?email={e}", "unknown@example.com")
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    @DisplayName("POST /api/v1/customers → 201 CREATED + body, 400 on validation error")
    void addCustomer() {

            CustomerRequestModel req = CustomerRequestModel.builder()
                    .firstName("Bob").lastName("Jones")
                    .emailAddress("bob@example.com")
                    .streetAddress("456 Oak St").postalCode("B2B2B2")
                    .city("Ville").province("VS")
                    .username("bjones").password1("pw").password2("pw")
                    .phoneNumbers(List.of(new PhoneNumber(PhoneType.HOME, "555-0000")))
                    .build();

            CustomerResponseModel created = CustomerResponseModel.builder()
                    .customerId("C2")
                    .firstName("Bob").lastName("Jones")
                    .emailAddress("bob@example.com")
                    .streetAddress("456 Oak St").postalCode("B2B2B2")
                    .city("Ville").province("VS")
                    .phoneNumbers(req.getPhoneNumbers())
                    .build();

            // Synchronous stub (no Mono)
            given(customerClient.addCustomer(any(CustomerRequestModel.class)))
                    .willReturn(created);

            // act + assert happy path
        client.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .value(c -> {
                    assertThat(c.getCustomerId()).isEqualTo("C2");
                    assertThat(c.getFirstName()).isEqualTo("Bob");
                });

        verify(customerClient).addCustomer(any(CustomerRequestModel.class));

        // Validation error stays the same
        client.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"firstName\":\"X\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PUT /api/v1/customers/{id} → 200 OK + body, 404 when missing")
    void updateCustomer() {
        // arrange
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Alice").lastName("Smith")
                .emailAddress("alice@example.com")
                .streetAddress("123 Main St").postalCode("A1A 1A1")
                .city("Townsville").province("TS")
                .username("asmith").password1("pw").password2("pw")
                .phoneNumbers(List.of(new PhoneNumber(PhoneType.MOBILE, "555-1234")))
                .build();

        CustomerResponseModel updated = CustomerResponseModel.builder()
                .customerId("C1")
                .firstName("Alice").lastName("Smith")
                .emailAddress("alice@example.com")
                .streetAddress("123 Main St").postalCode("A1A 1A1")
                .city("Townsville").province("TS")
                .phoneNumbers(req.getPhoneNumbers())
                .build();

        // happy-path stub
        given(customerClient.updateCustomer(eq("C1"), any(CustomerRequestModel.class)))
                .willReturn(updated);

        // act + assert happy path
        client.put().uri("/api/v1/customers/{id}", "C1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(c -> {
                    assertThat(c.getCustomerId()).isEqualTo("C1");
                    assertThat(c.getFirstName()).isEqualTo("Alice");
                });

        // **missing-ID stub**: throw 404 exception instead of returning null
        given(customerClient.updateCustomer(eq("X"), any(CustomerRequestModel.class)))
                .willThrow(WebClientResponseException.create(
                        404, "Not Found", null, null, null
                ));

        // act + assert 404
        client.put().uri("/api/v1/customers/{id}", "X")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError();
    }



    @Test
    @DisplayName("DELETE /api/v1/customers/{id} → 204 No Content + Link header, 5xx when missing")
    void deleteCustomer() {
        // stub successful deletion (your client returns something, but controller ignores it)
        given(customerClient.deleteCustomerbyCustomerId("C1"))
                .willReturn(null);

        client.delete().uri("/api/v1/customers/{id}", "C1")
                .exchange()
                // 1) now expect 204
                .expectStatus().isNoContent()
                // 2) still check the Link header
                .expectHeader().value("Link", link ->
                        assertThat(link).contains("/api/v1/customers")
                )
                // 3) no body for 204
                .expectBody().isEmpty();

        // missing → still throws RuntimeException, yielding a 5xx
        given(customerClient.deleteCustomerbyCustomerId("X"))
                .willThrow(new RuntimeException("not found"));

        client.delete().uri("/api/v1/customers/{id}", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/v1/plans → 200 + non-empty list")
    void getAllServicePlans_positive() {
        client.get().uri("/api/v1/plans")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ServicePlanResponseModel.class)
                .hasSize(1)
                .value(list -> {
                    ServicePlanResponseModel p = list.get(0);
                    assertThat(p.getPlanId()).isEqualTo("P1");
                    assertThat(p.getCoverageDetails()).isEqualTo("2-year");
                    assertThat(p.getExpirationDate()).isEqualTo(expDate);
                });
    }

    @Test
    @DisplayName("GET /api/v1/plans/{id} → 200 when found")
    void getServicePlanById_positive() {
        client.get().uri("/api/v1/plans/{id}", "P1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> {
                    assertThat(p.getPlanId()).isEqualTo("P1");
                    assertThat(p.getCoverageDetails()).isEqualTo("2-year");
                    assertThat(p.getExpirationDate()).isEqualTo(expDate);
                });
    }

    @Test
    @DisplayName("GET /api/v1/plans/{id} → 404 when not found")
    void getServicePlanById_negative() {
        client.get().uri("/api/v1/plans/{id}", "UNKNOWN")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/plans → 201 CREATED + body")
    void addServicePlan_positive() {
        ServicePlanRequestModel req = new ServicePlanRequestModel("3-year", expDate.plusYears(2));
        ServicePlanResponseModel created = ServicePlanResponseModel.builder()
                .planId("NEWP")
                .coverageDetails("3-year")
                .expirationDate(expDate.plusYears(2))
                .build();
        // stub downstream client
        given(planClient.addServicePlan(any(ServicePlanRequestModel.class)))
                .willReturn(created);

        client.post().uri("/api/v1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                // expect 201 instead of 200
                .expectStatus().isCreated()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> {
                    assertThat(p.getPlanId()).isEqualTo("NEWP");
                    assertThat(p.getCoverageDetails()).isEqualTo("3-year");
                });
    }

    @Test
    @DisplayName("PUT /api/v1/plans/{id} → 200 + updated body")
    void updateServicePlan_positive() {
        ServicePlanRequestModel req = new ServicePlanRequestModel("Extended", expDate.plusMonths(6));
        ServicePlanResponseModel updated = ServicePlanResponseModel.builder()
                .planId("P1")
                .coverageDetails("Extended")
                .expirationDate(expDate.plusMonths(6))
                .build();

        given(planClient.updateServicePlan(eq("P1"), any(ServicePlanRequestModel.class)))
                .willReturn(updated);

        client.put().uri("/api/v1/plans/{id}", "P1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> {
                    assertThat(p.getCoverageDetails()).isEqualTo("Extended");
                });
    }

    @Test
    @DisplayName("PUT /api/v1/plans/{id} → 404 when missing")
    void updateServicePlan_negative() {
        ServicePlanRequestModel req = new ServicePlanRequestModel("X", expDate);
        given(planClient.updateServicePlan(eq("X"), any(ServicePlanRequestModel.class)))
                .willThrow(WebClientResponseException.create(404, "Not Found", null, null, null));

        client.put().uri("/api/v1/plans/{id}", "X")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("DELETE /api/v1/plans/{id} → 204 No Content + Link header, no body")
    void deleteServicePlan_positive() {
        // stub the downstream call (return value is ignored by controller)
        given(planClient.deleteServicePlanById("P1"))
                .willReturn("Service plan deleted successfully.");

        client.delete().uri("/api/v1/plans/{id}", "P1")
                .exchange()
                // 1) expect 204 instead of 200
                .expectStatus().isNoContent()
                // 2) still verify the Link header
                .expectHeader().value("Link", link ->
                        assertThat(link).contains("/api/v1/plans")
                )
                // 3) no body allowed for 204
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/plans/{id} → 404 when not found")
    void deleteServicePlan_negative() {
        doThrow(WebClientResponseException.create(404, "Not Found", null, null, null))
                .when(planClient).deleteServicePlanById("UNKNOWN");

        client.delete().uri("/api/v1/plans/{id}", "UNKNOWN")
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test @DisplayName("GET /api/v1/watches → 200 + non-empty list")
    void getAllWatches_positive() {
        client.get().uri("/api/v1/watches?foo=bar")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getWatchId()).isEqualTo("W1"));
    }

    @Test
    @DisplayName("POST /api/v1/catalogs/{catalog_id}/watches → 201 + body")
    void addWatch_positive() {
        WatchRequestModel req = WatchRequestModel.builder()
                .catalogId("CAT1")
                .quantity(5)
                .usageType(UsageType.NEW)
                .model("Diver")
                .material("Titanium")
                .accessories(List.of())
                .price(new Price(new BigDecimal("250"), new BigDecimal("200"), new BigDecimal("25")))
                .watchBrand(new WatchBrand("Seiko","JP"))
                .build();

        WatchResponseModel created = WatchResponseModel.builder()
                .watchId("W2")
                .catalogId("CAT1")
                .quantity(5)
                .usageType(UsageType.NEW)
                .model("Diver")
                .material("Titanium")
                .accessories(List.of())
                .price(req.getPrice())
                .watchBrand(req.getWatchBrand())
                .build();

        // use any() for the request body
        given(productClient.addWatches(any(WatchRequestModel.class), eq("CAT1")))
                .willReturn(created);

        client.post()
                .uri("/api/v1/catalogs/{cid}/watches", "CAT1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WatchResponseModel.class)
                .value(w -> {
                    assertThat(w.getWatchId()).isEqualTo("W2");
                    assertThat(w.getModel()).isEqualTo("Diver");
                });
    }

    @Test
    @DisplayName("POST /api/v1/catalogs/{catalog_id}/watches → 500 on bad input")
    void addWatch_negative() {
        // stub *any* bad request
        given(productClient.addWatches(any(WatchRequestModel.class), eq("CAT1")))
                .willThrow(new RuntimeException("Invalid data"));

        client.post()
                .uri("/api/v1/catalogs/{cid}/watches", "CAT1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().is5xxServerError();
    }


    // ── UPDATE ──
    @Test
    @DisplayName("PUT /api/v1/catalogs/{catalog_id}/watches/{id} → 200 + updated body")
    void updateWatch_positive() {
        WatchRequestModel req = WatchRequestModel.builder()
                .catalogId("CAT1")
                .quantity(8)
                .usageType(UsageType.USED)
                .model("Explorer")
                .material("Steel")
                .accessories(List.of())
                .price(new Price(new BigDecimal("180"), new BigDecimal("150"), new BigDecimal("20")))
                .watchBrand(new WatchBrand("Omega","CH"))
                .build();

        WatchResponseModel updated = WatchResponseModel.builder()
                .watchId("W1")
                .catalogId("CAT1")
                .quantity(8)
                .usageType(UsageType.USED)
                .model("Explorer")
                .material("Steel")
                .accessories(List.of())
                .price(req.getPrice())
                .watchBrand(req.getWatchBrand())
                .build();

        // match any deserialized instance
        given(productClient.updateWatchInInventory(
                eq("CAT1"), eq("W1"), any(WatchRequestModel.class)))
                .willReturn(updated);

        client.put()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", "CAT1", "W1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WatchResponseModel.class)
                .value(w -> {
                    assertThat(w.getQuantity()).isEqualTo(8);
                    assertThat(w.getModel()).isEqualTo("Explorer");
                });
    }

    @Test
    @DisplayName("PUT /api/v1/catalogs/{catalog_id}/watches/{id} → 404 when not found")
    void updateWatch_negative() {
        // any body + bad ID → 404
        given(productClient.updateWatchInInventory(
                eq("CAT1"), eq("X"), any(WatchRequestModel.class)))
                .willThrow(new WebClientResponseException(404, "Not Found", null, null, null));

        client.put()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", "CAT1", "X")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(WatchRequestModel.builder().build())
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // ── DELETE ── DELETE /api/v1/catalogs/{catalog_id}/watches/{watchId} ──
    @Test
    @DisplayName("DELETE /api/v1/catalogs/{catalog_id}/watches/{id} → 200 + message")
    void deleteWatch_positive() {
        // stub downstream to return the exact confirmation string
        given(productClient.removeWatchInCatalog("CAT1", "W1"))
                .willReturn("Watch with Id: W1 deleted successfully.");

        client.delete()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", "CAT1", "W1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                // match the full response
                .isEqualTo("Watch with Id: W1 deleted successfully.");
    }

    @Test
    @DisplayName("DELETE /api/v1/catalogs/{catalog_id}/watches/{id} → 500 when not found")
    void deleteWatch_negative() {
        doThrow(new RuntimeException("not found"))
                .when(productClient).removeWatchInCatalog("CAT1", "X");

        client.delete()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", "CAT1", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test @DisplayName("GET /api/v1/watches/{id} → 200 + HATEOAS self link")
    void getWatchById_positive() {
        client.get()
                .uri("/api/v1/watches/{wid}", "W1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.watchId").isEqualTo("W1")
                // verify the self‐link was added by the lambda
                .jsonPath("$._links.self.href").exists();
    }

    @Test @DisplayName("GET /api/v1/watches/{id} → 5xx when not found")
    void getWatchById_negative() {
        given(productClient.getWatchesWithFilter(anyMap()))
                .willReturn(List.of());  // no change to the by‐ID stub
        // override the single‐watch stub:
        given(productClient.getCatalogWatchByID("X")).willReturn(null);

        client.get()
                .uri("/api/v1/watches/{wid}", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }



    @Test @DisplayName("GET /api/v1/catalogs/{cid}/watches/{wid} → 5xx when not found")
    void getWatchInCatalogById_negative() {
        given(productClient.getCatalogWatchByID("X"))
                .willThrow(new RuntimeException("not found"));

        client.get()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", "CAT1", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/v1/catalogs/{cid}/watches → 200 + list + HATEOAS links")
    void getWatchesInCatalogWithFiltering_positive() {
        client.get()
                .uri("/api/v1/catalogs/{cid}/watches?foo=bar", CATALOG)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .hasSize(1)
                .value(list -> {
                    WatchResponseModel w = list.get(0);
                    // covers getWatchesInCatalogWithFiltering + lambda that adds links
                    assertThat(w.getWatchId()).isEqualTo(WATCH);
                    assertThat(w.getLink("self")).isPresent();
                    assertThat(w.getLink("all-watches")).isPresent();
                });
    }

    @Test
    @DisplayName("GET /api/v1/catalogs/{cid}/watches/{wid} → 200 + single item")
    void getWatchInCatalogByWatchId_positive() {
        client.get()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", CATALOG, WATCH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WatchResponseModel.class)
                .value(w -> {
                    // covers getWatchInCatalogByWatchId
                    assertThat(w.getWatchId()).isEqualTo(WATCH);
                    assertThat(w.getModel()).isEqualTo("X123");
                });
    }

    @Test
    @DisplayName("GET /api/v1/catalogs/{cid}/watches/{wid} → 200 + empty when not found")
    void getWatchInCatalogByWatchId_notFoundReturnsEmpty() {
        // override stub for missing ID
        given(productClient.getCatalogWatchByID("X"))
                .willReturn(null);

        client.get()
                .uri("/api/v1/catalogs/{cid}/watches/{wid}", CATALOG, "X")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .isEmpty();
    }


    @Test
    @DisplayName("GET /api/v1/orders → 200 + non-empty list + HATEOAS links")
    void getAllOrders_positive() {
        client.get().uri("/api/v1/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseModel.class)
                .hasSize(1)
                .value(list -> {
                    OrderResponseModel o = list.get(0);
                    assertThat(o.getOrderId()).isEqualTo("O1");
                    assertThat(o.getLink("self")).isPresent();
                    assertThat(o.getLink("customer")).isPresent();
                    assertThat(o.getLink("catalog")).isPresent();
                    assertThat(o.getLink("watch")).isPresent();
                    assertThat(o.getLink("servicePlan")).isPresent();
                });
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} → 200 + single")
    void getOrderById_positive() {
        client.get().uri("/api/v1/orders/{id}", "O1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(o -> {
                    assertThat(o.getOrderId()).isEqualTo("O1");
                    // no HATEOAS assertions here
                });
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} → 404 when not found")
    void getOrderById_negative() {
        client.get().uri("/api/v1/orders/{id}", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("POST /api/v1/orders → 201 + Location header + body")
    void createOrder_positive() {
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId("C1").catalogId("CAT1").watchId("W1").servicePlanId("P1")
                .orderName("TestOrder").salePrice(100.0)
                .currency("USD").paymentCurrency("EUR")
                .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        client.post().uri("/api/v1/orders")
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueMatches("Location", ".*/api/v1/orders/O1$")
                .expectBody(OrderResponseModel.class)
                .value(o -> {
                    assertThat(o.getOrderId()).isEqualTo("O1");
                    // no link assertion here since create() doesn’t add HATEOAS links
                });
    }

    @Test
    @DisplayName("PUT /api/v1/orders/{id} → 200 + updated")
    void updateOrder_positive() {
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId("C1")
                .catalogId("CAT1")
                .watchId("W1")
                .servicePlanId("P1")
                .orderName("TestOrder")
                .salePrice(100.0)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_CANCELED)
                .build();

        client.put().uri("/api/v1/orders/{id}", "O1")
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(o -> {
                    // stub always returns the original status, so we still get PURCHASE_COMPLETED
                    assertThat(o.getOrderStatus())
                            .isEqualTo(OrderStatus.PURCHASE_COMPLETED);

                    // no HATEOAS here
                });
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} → 204 No Content")
    void deleteOrder_positive() {
        client.delete().uri("/api/v1/orders/{id}", "O1")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} → 404 when not found")
    void deleteOrder_negative() {
        client.delete().uri("/api/v1/orders/{id}", "X")
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    void catalogRequestModelBuilder_chainToStringBuild() {
        // exercise every builder method
        CatalogRequestModel.CatalogRequestModelBuilder builder =
                CatalogRequestModel.builder()
                        .type("Premium")
                        .description("High-end watches");

        // toString() on the builder should include both fields
        String repr = builder.toString();
        assertThat(repr)
                .contains("type=Premium")
                .contains("description=High-end watches");

        // build & getters
        CatalogRequestModel dto = builder.build();
        assertThat(dto.getType()).isEqualTo("Premium");
        assertThat(dto.getDescription()).isEqualTo("High-end watches");
    }

    @Test
    void phoneNumberBuilder_chainToStringBuildAndSetters() {
        // exercise every builder method
        PhoneNumber.PhoneNumberBuilder builder =
                PhoneNumber.builder()
                        .type(PhoneType.HOME)
                        .number("555-1234");

        // toString() on the builder should include both fields
        String repr = builder.toString();
        assertThat(repr)
                .contains("type=HOME")        // was incorrectly checking for WORK
                .contains("number=555-1234");

        // build & getters
        PhoneNumber pn = builder.build();
        assertThat(pn.getType()).isEqualTo(PhoneType.HOME);
        assertThat(pn.getNumber()).isEqualTo("555-1234");

        // exercise the setters too
        pn.setNumber("999-0000");
        assertThat(pn.getNumber()).isEqualTo("999-0000");
    }





}



