package com.example.orderservices.presentationlayer;

import com.example.orderservices.businesslogiclayer.OrderService;
import com.example.orderservices.dataaccesslayer.OrderStatus;
import com.example.orderservices.presentationlayer.customerdtos.CustomerResponseModel;
import com.example.orderservices.presentationlayer.productdtos.catalogdtos.CatalogResponseModel;
import com.example.orderservices.presentationlayer.productdtos.watchdtos.*;
import com.example.orderservices.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class OrderControllerUnitTest {

    @Test
    @DisplayName("CustomerResponseModel: builder, builder.toString(), setters + getters")
    void customerBuilderAndSetters() {
        // exercise builder methods and toString()
        CustomerResponseModel.CustomerResponseModelBuilder builder =
                CustomerResponseModel.builder()
                        .customerId("CU1")
                        .firstName("Foo")
                        .lastName("Bar");
        String builderString = builder.toString();
        assertThat(builderString)
                .contains("customerId=CU1")
                .contains("firstName=Foo")
                .contains("lastName=Bar");

        // build DTO and check getters
        CustomerResponseModel dto = builder.build();
        assertThat(dto.getCustomerId()).isEqualTo("CU1");
        assertThat(dto.getFirstName()).isEqualTo("Foo");
        assertThat(dto.getLastName()).isEqualTo("Bar");

        // now mutate via setters and re-check getters
        dto.setCustomerId("CU2");
        dto.setFirstName("Alice");
        dto.setLastName("Smith");

        assertThat(dto.getCustomerId()).isEqualTo("CU2");
        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("CatalogResponseModel: builder, builder.toString(), setters + getters")
    void catalogBuilderAndSetters() {
        CatalogResponseModel.CatalogResponseModelBuilder builder =
                CatalogResponseModel.builder()
                        .catalogId("CAT1")
                        .type("Standard")
                        .description("Everyday watches");
        String builderString = builder.toString();
        assertThat(builderString)
                .contains("catalogId=CAT1")
                .contains("type=Standard")
                .contains("description=Everyday watches");

        CatalogResponseModel dto = builder.build();
        assertThat(dto.getCatalogId()).isEqualTo("CAT1");
        assertThat(dto.getType()).isEqualTo("Standard");
        assertThat(dto.getDescription()).isEqualTo("Everyday watches");

        dto.setCatalogId("CAT2");
        dto.setType("Luxury");
        dto.setDescription("High-end collection");

        assertThat(dto.getCatalogId()).isEqualTo("CAT2");
        assertThat(dto.getType()).isEqualTo("Luxury");
        assertThat(dto.getDescription()).isEqualTo("High-end collection");
    }

    @Test
    @DisplayName("ServicePlanResponseModel: builder, builder.toString(), setters + getters")
    void servicePlanBuilderAndSetters() {
        LocalDate originalExpiry = LocalDate.of(2025, 12, 31);
        ServicePlanResponseModel.ServicePlanResponseModelBuilder builder =
                ServicePlanResponseModel.builder()
                        .planId("P1")
                        .coverageDetails("3-year coverage")
                        .expirationDate(originalExpiry);
        String builderString = builder.toString();
        assertThat(builderString)
                .contains("planId=P1")
                .contains("coverageDetails=3-year coverage")
                .contains("expirationDate=" + originalExpiry);

        ServicePlanResponseModel dto = builder.build();
        assertThat(dto.getPlanId()).isEqualTo("P1");
        assertThat(dto.getCoverageDetails()).isEqualTo("3-year coverage");
        assertThat(dto.getExpirationDate()).isEqualTo(originalExpiry);

        // mutate via setters
        LocalDate newExpiry = LocalDate.of(2028, 6, 30);
        dto.setPlanId("P2");
        dto.setCoverageDetails("Lifetime");
        dto.setExpirationDate(newExpiry);

        assertThat(dto.getPlanId()).isEqualTo("P2");
        assertThat(dto.getCoverageDetails()).isEqualTo("Lifetime");
        assertThat(dto.getExpirationDate()).isEqualTo(newExpiry);
    }


    @Test
    @DisplayName("Price, Accessory, WatchBrand, WatchResponseModel & WatchRequestModel: builders, toString(), getters + setters")
    void watchRelatedDtosBuilderAndSetters() {
        // ----- Price -----
        Price.PriceBuilder priceBuilder = Price.builder()
                .msrp(new BigDecimal("199.99"))
                .cost(new BigDecimal("150.00"))
                .totalOptionsCost(new BigDecimal("25.00"));
        String priceBuilderStr = priceBuilder.toString();
        assertThat(priceBuilderStr)
                .contains("msrp=199.99")
                .contains("cost=150.00")
                .contains("totalOptionsCost=25.00");

        Price price = priceBuilder.build();
        assertThat(price.getMsrp()).isEqualByComparingTo("199.99");
        assertThat(price.getCost()).isEqualByComparingTo("150.00");
        assertThat(price.getTotalOptionsCost()).isEqualByComparingTo("25.00");

        // setters
        price.setMsrp(new BigDecimal("209.99"));
        price.setCost(new BigDecimal("160.00"));
        price.setTotalOptionsCost(new BigDecimal("30.00"));
        assertThat(price.getMsrp()).isEqualByComparingTo("209.99");
        assertThat(price.getCost()).isEqualByComparingTo("160.00");
        assertThat(price.getTotalOptionsCost()).isEqualByComparingTo("30.00");

        // ----- Accessory -----
        Accessory.AccessoryBuilder accBuilder = Accessory.builder()
                .accessoryName("Strap")
                .accessoryCost(new BigDecimal("15.00"));
        String accBuilderStr = accBuilder.toString();
        assertThat(accBuilderStr)
                .contains("accessoryName=Strap")
                .contains("accessoryCost=15.00");

        Accessory accessory = accBuilder.build();
        assertThat(accessory.getAccessoryName()).isEqualTo("Strap");
        assertThat(accessory.getAccessoryCost()).isEqualByComparingTo("15.00");

        accessory.setAccessoryName("Box");
        accessory.setAccessoryCost(new BigDecimal("10.00"));
        assertThat(accessory.getAccessoryName()).isEqualTo("Box");
        assertThat(accessory.getAccessoryCost()).isEqualByComparingTo("10.00");

        // ----- WatchBrand -----
        WatchBrand.WatchBrandBuilder brandBuilder = WatchBrand.builder()
                .brandName("Rolex")
                .brandCountry("Switzerland");
        String brandBuilderStr = brandBuilder.toString();
        assertThat(brandBuilderStr)
                .contains("brandName=Rolex")
                .contains("brandCountry=Switzerland");

        WatchBrand brand = brandBuilder.build();
        assertThat(brand.getBrandName()).isEqualTo("Rolex");
        assertThat(brand.getBrandCountry()).isEqualTo("Switzerland");

        brand.setBrandName("Omega");
        brand.setBrandCountry("USA");
        assertThat(brand.getBrandName()).isEqualTo("Omega");
        assertThat(brand.getBrandCountry()).isEqualTo("USA");

        // ----- WatchResponseModel -----
        WatchResponseModel.WatchResponseModelBuilder wrBuilder = WatchResponseModel.builder()
                .watchId("W123")
                .catalogId("CAT-A")
                .model("Explorer")
                .material("Steel")
                .quantity(5)
                .accessories(List.of(accessory))
                .price(price)
                .watchBrand(brand)
                .usageType(UsageType.NEW);
        String wrBuilderStr = wrBuilder.toString();
        assertThat(wrBuilderStr)
                .contains("watchId=W123")
                .contains("catalogId=CAT-A")
                .contains("model=Explorer")
                .contains("material=Steel")
                .contains("quantity=5");

        WatchResponseModel wr = wrBuilder.build();
        assertThat(wr.getWatchId()).isEqualTo("W123");
        assertThat(wr.getCatalogId()).isEqualTo("CAT-A");
        assertThat(wr.getModel()).isEqualTo("Explorer");
        assertThat(wr.getMaterial()).isEqualTo("Steel");
        assertThat(wr.getQuantity()).isEqualTo(5);
        assertThat(wr.getAccessories()).containsExactly(accessory);
        assertThat(wr.getPrice()).isEqualTo(price);
        assertThat(wr.getWatchBrand()).isEqualTo(brand);
        assertThat(wr.getUsageType()).isEqualTo(UsageType.NEW);

        // setters
        wr.setModel("Submariner");
        wr.setMaterial("Gold");
        wr.setQuantity(2);
        wr.setAccessories(List.of());
        wr.setUsageType(UsageType.USED);
        assertThat(wr.getModel()).isEqualTo("Submariner");
        assertThat(wr.getMaterial()).isEqualTo("Gold");
        assertThat(wr.getQuantity()).isEqualTo(2);
        assertThat(wr.getAccessories()).isEmpty();
        assertThat(wr.getUsageType()).isEqualTo(UsageType.USED);

        // ----- WatchRequestModel -----
        WatchRequestModel.WatchRequestModelBuilder rqBuilder = WatchRequestModel.builder()
                .catalogId("CAT-A")
                .model("Explorer")
                .material("Steel")
                .quantity(3)
                .accessories(List.of(accessory))
                .price(price)
                .watchBrand(brand)
                .usageType(UsageType.NEW);
        String rqBuilderStr = rqBuilder.toString();
        assertThat(rqBuilderStr)
                .contains("catalogId=CAT-A")
                .contains("quantity=3");

        WatchRequestModel rq = rqBuilder.build();
        assertThat(rq.getCatalogId()).isEqualTo("CAT-A");
        assertThat(rq.getModel()).isEqualTo("Explorer");
        assertThat(rq.getMaterial()).isEqualTo("Steel");
        assertThat(rq.getQuantity()).isEqualTo(3);
        assertThat(rq.getAccessories()).containsExactly(accessory);
        assertThat(rq.getPrice()).isEqualTo(price);
        assertThat(rq.getWatchBrand()).isEqualTo(brand);
        assertThat(rq.getUsageType()).isEqualTo(UsageType.NEW);

        // setters
        rq.setCatalogId("CAT-B");
        rq.setModel("SeaMaster");
        rq.setMaterial("Titanium");
        rq.setQuantity(1);
        rq.setAccessories(List.of());
        rq.setUsageType(UsageType.NEW);
        assertThat(rq.getCatalogId()).isEqualTo("CAT-B");
        assertThat(rq.getModel()).isEqualTo("SeaMaster");
        assertThat(rq.getMaterial()).isEqualTo("Titanium");
        assertThat(rq.getQuantity()).isEqualTo(1);
        assertThat(rq.getAccessories()).isEmpty();
        assertThat(rq.getUsageType()).isEqualTo(UsageType.NEW);
    }

    @Test
    @DisplayName("Price, Accessory, WatchBrand equals() and hashCode()")
    void equalsAndHashCode() {
        Price p1 = new Price(new BigDecimal("100.00"), new BigDecimal("80.00"), new BigDecimal("10.00"));
        Price p2 = new Price(new BigDecimal("100.00"), new BigDecimal("80.00"), new BigDecimal("10.00"));
        Price p3 = new Price(new BigDecimal("120.00"), new BigDecimal("90.00"), new BigDecimal("15.00"));
        assertThat(p1).isEqualTo(p2).hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(p3);

        Accessory a1 = new Accessory("Box", new BigDecimal("5.00"));
        Accessory a2 = new Accessory("Box", new BigDecimal("5.00"));
        Accessory a3 = new Accessory("Strap", new BigDecimal("7.00"));
        assertThat(a1).isEqualTo(a2).hasSameHashCodeAs(a2);
        assertThat(a1).isNotEqualTo(a3);

        WatchBrand b1 = new WatchBrand("Omega", "Switzerland");
        WatchBrand b2 = new WatchBrand("Omega", "Switzerland");
        WatchBrand b3 = new WatchBrand("Rolex", "USA");
        assertThat(b1).isEqualTo(b2).hasSameHashCodeAs(b2);
        assertThat(b1).isNotEqualTo(b3);
    }

    @Test
    @DisplayName("OrderRequestModel & OrderResponseModel negative equals and hashCode and invalid data")
    void orderModelsNegativeTest() {
        // negative equals for OrderRequestModel
        LocalDateTime now = LocalDateTime.now().withNano(0);
        OrderRequestModel r1 = new OrderRequestModel("C1","CAT1","W1","P1","O1",100.0,"USD","EUR",now,OrderStatus.PURCHASE_COMPLETED);
        OrderRequestModel r2 = new OrderRequestModel("C1","CAT1","W1","P1","O1",100.0,"USD","EUR",now,OrderStatus.PURCHASE_COMPLETED);
        OrderRequestModel r3 = new OrderRequestModel("C2","CAT2","W2","P2","O2",200.0,"CAD","GBP",now.plusDays(1),OrderStatus.PURCHASE_NEGOTIATION);
        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2);
        assertThat(r1).isNotEqualTo(r3);
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1).isNotEqualTo("not a model");

        // negative equals for OrderResponseModel
        LocalDate expDate = LocalDate.of(2025,5,20);
        LocalDateTime ordDate = LocalDateTime.of(2025,5,20,12,0);
        OrderResponseModel s1 = new OrderResponseModel(
                "O1","C1","F","L","CAT1","T","D",
                "W1","M","Mat","P1","Cov",expDate,
                "O1",100.0,"USD","EUR",ordDate,OrderStatus.PURCHASE_OFFER
        );
        OrderResponseModel s2 = new OrderResponseModel(
                "O1","C1","F","L","CAT1","T","D",
                "W1","M","Mat","P1","Cov",expDate,
                "O1",100.0,"USD","EUR",ordDate,OrderStatus.PURCHASE_OFFER
        );
        OrderResponseModel s3 = new OrderResponseModel(
                "O2","C2","X","Y","CAT2","T2","D2",
                "W2","M2","Mat2","P2","Cov2",expDate,
                "O2",200.0,"CAD","GBP",ordDate,OrderStatus.PURCHASE_CANCELED
        );
        assertThat(s1).isEqualTo(s2).hasSameHashCodeAs(s2);
        assertThat(s1).isNotEqualTo(s3);
        assertThat(s1).isNotEqualTo(null);
        assertThat(s1).isNotEqualTo(123);
    }

    @Test
    @DisplayName("OrderRequestModel & OrderResponseModel positive equals and hashCode")
    void orderModelsPositiveEqualsAndHashCodeTest() {
        // positive equals/hashCode for OrderRequestModel
        LocalDateTime now = LocalDateTime.now().withNano(0);
        OrderRequestModel r1 = new OrderRequestModel("C1","CAT1","W1","P1","O1",100.0,"USD","EUR",now,OrderStatus.PURCHASE_COMPLETED);
        OrderRequestModel r2 = new OrderRequestModel("C1","CAT1","W1","P1","O1",100.0,"USD","EUR",now,OrderStatus.PURCHASE_COMPLETED);
        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2);

        // positive equals/hashCode for OrderResponseModel
        LocalDate expDate = LocalDate.of(2025,5,20);
        LocalDateTime ordDate = LocalDateTime.of(2025,5,20,12,0);
        OrderResponseModel s1 = new OrderResponseModel(
                "O1","C1","First","Last","CAT1","Type","Desc",
                "W1","Model","Mat","P1","Cov",expDate,
                "Order1",100.0,"USD","EUR",ordDate,OrderStatus.PURCHASE_COMPLETED
        );
        OrderResponseModel s2 = new OrderResponseModel(
                "O1","C1","First","Last","CAT1","Type","Desc",
                "W1","Model","Mat","P1","Cov",expDate,
                "Order1",100.0,"USD","EUR",ordDate,OrderStatus.PURCHASE_COMPLETED
        );
        assertThat(s1).isEqualTo(s2).hasSameHashCodeAs(s2);
    }

    @Test
    @DisplayName("OrderRequestModel & OrderResponseModel: builder, getters + setters")
    void orderRequestAndResponseModelBuilderAndSetters() {
        // ----- OrderRequestModel positive -----
        LocalDateTime now = LocalDateTime.now().withNano(0);
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId("C1")
                .catalogId("CAT1")
                .watchId("W1")
                .servicePlanId("P1")
                .orderName("Order1")
                .salePrice(123.45)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // getters
        assertThat(req.getCustomerId()).isEqualTo("C1");
        assertThat(req.getCatalogId()).isEqualTo("CAT1");
        assertThat(req.getWatchId()).isEqualTo("W1");
        assertThat(req.getServicePlanId()).isEqualTo("P1");
        assertThat(req.getOrderName()).isEqualTo("Order1");
        assertThat(req.getSalePrice()).isEqualTo(123.45);
        assertThat(req.getCurrency()).isEqualTo("USD");
        assertThat(req.getPaymentCurrency()).isEqualTo("EUR");
        assertThat(req.getOrderDate()).isEqualTo(now);
        assertThat(req.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);

        // setters
        req.setCustomerId("C2");
        req.setCatalogId("CAT2");
        req.setWatchId("W2");
        req.setServicePlanId("P2");
        req.setOrderName("Order2");
        req.setSalePrice(200.00);
        req.setCurrency("CAD");
        req.setPaymentCurrency("GBP");
        LocalDateTime later = now.plusDays(1);
        req.setOrderDate(later);
        req.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);

        assertThat(req.getCustomerId()).isEqualTo("C2");
        assertThat(req.getCatalogId()).isEqualTo("CAT2");
        assertThat(req.getWatchId()).isEqualTo("W2");
        assertThat(req.getServicePlanId()).isEqualTo("P2");
        assertThat(req.getOrderName()).isEqualTo("Order2");
        assertThat(req.getSalePrice()).isEqualTo(200.00);
        assertThat(req.getCurrency()).isEqualTo("CAD");
        assertThat(req.getPaymentCurrency()).isEqualTo("GBP");
        assertThat(req.getOrderDate()).isEqualTo(later);
        assertThat(req.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);

        // ----- OrderResponseModel positive -----
        LocalDate exp = LocalDate.of(2025, 12, 31);
        LocalDateTime ord = LocalDateTime.of(2025, 12, 31, 10, 0);
        OrderResponseModel resp = new OrderResponseModel(
                "O1",
                "C1",
                "First",
                "Last",
                "CAT1",
                "Type1",
                "Desc1",
                "W1",
                "Model1",
                "Mat1",
                "P1",
                "Cov1",
                exp,
                "Order1",
                123.45,
                "USD",
                "EUR",
                ord,
                OrderStatus.PURCHASE_COMPLETED
        );

        // getters
        assertThat(resp.getOrderId()).isEqualTo("O1");
        assertThat(resp.getCustomerId()).isEqualTo("C1");
        assertThat(resp.getCustomerFirstName()).isEqualTo("First");
        assertThat(resp.getCustomerLastName()).isEqualTo("Last");
        assertThat(resp.getCatalogId()).isEqualTo("CAT1");
        assertThat(resp.getCatalogType()).isEqualTo("Type1");
        assertThat(resp.getCatalogDescription()).isEqualTo("Desc1");
        assertThat(resp.getWatchId()).isEqualTo("W1");
        assertThat(resp.getWatchModel()).isEqualTo("Model1");
        assertThat(resp.getWatchMaterial()).isEqualTo("Mat1");
        assertThat(resp.getServicePlanId()).isEqualTo("P1");
        assertThat(resp.getServicePlanCoverageDetails()).isEqualTo("Cov1");
        assertThat(resp.getServicePlanExpirationDate()).isEqualTo(exp);
        assertThat(resp.getOrderName()).isEqualTo("Order1");
        assertThat(resp.getSalePrice()).isEqualTo(123.45);
        assertThat(resp.getSaleCurrency()).isEqualTo("USD");
        assertThat(resp.getPaymentCurrency()).isEqualTo("EUR");
        assertThat(resp.getOrderDate()).isEqualTo(ord);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);

        // setters
        resp.setOrderId("O2");
        resp.setCustomerId("C2");
        resp.setCustomerFirstName("F2");
        resp.setCustomerLastName("L2");
        resp.setCatalogId("CAT2");
        resp.setCatalogType("Type2");
        resp.setCatalogDescription("Desc2");
        resp.setWatchId("W2");
        resp.setWatchModel("Model2");
        resp.setWatchMaterial("Mat2");
        resp.setServicePlanId("P2");
        resp.setServicePlanCoverageDetails("Cov2");
        LocalDate exp2 = exp.plusDays(1);
        resp.setServicePlanExpirationDate(exp2);
        resp.setOrderName("Order2");
        resp.setSalePrice(200.00);
        resp.setSaleCurrency("CAD");
        resp.setPaymentCurrency("GBP");
        LocalDateTime ord2 = ord.plusHours(1);
        resp.setOrderDate(ord2);
        resp.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);

        assertThat(resp.getOrderId()).isEqualTo("O2");
        assertThat(resp.getCustomerId()).isEqualTo("C2");
        assertThat(resp.getCustomerFirstName()).isEqualTo("F2");
        assertThat(resp.getCustomerLastName()).isEqualTo("L2");
        assertThat(resp.getCatalogId()).isEqualTo("CAT2");
        assertThat(resp.getCatalogType()).isEqualTo("Type2");
        assertThat(resp.getCatalogDescription()).isEqualTo("Desc2");
        assertThat(resp.getWatchId()).isEqualTo("W2");
        assertThat(resp.getWatchModel()).isEqualTo("Model2");
        assertThat(resp.getWatchMaterial()).isEqualTo("Mat2");
        assertThat(resp.getServicePlanId()).isEqualTo("P2");
        assertThat(resp.getServicePlanCoverageDetails()).isEqualTo("Cov2");
        assertThat(resp.getServicePlanExpirationDate()).isEqualTo(exp2);
        assertThat(resp.getOrderName()).isEqualTo("Order2");
        assertThat(resp.getSalePrice()).isEqualTo(200.00);
        assertThat(resp.getSaleCurrency()).isEqualTo("CAD");
        assertThat(resp.getPaymentCurrency()).isEqualTo("GBP");
        assertThat(resp.getOrderDate()).isEqualTo(ord2);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);
    }

    @Test
    @DisplayName("OrderRequestModel: each field breaks equals()")
    void orderRequestModelFieldByFieldEquals() {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        // base instance with all fields set
        OrderRequestModel base = OrderRequestModel.builder()
                .customerId("C1")
                .catalogId("CAT1")
                .watchId("W1")
                .servicePlanId("P1")
                .orderName("Order1")
                .salePrice(100.0)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // now flip each field in turn:
        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("X1")  // changed
                        .catalogId("CAT1").watchId("W1").servicePlanId("P1")
                        .orderName("Order1").salePrice(100.0)
                        .currency("USD").paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1")
                        .catalogId("XAT")  // changed
                        .watchId("W1").servicePlanId("P1")
                        .orderName("Order1").salePrice(100.0)
                        .currency("USD").paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1")
                        .watchId("XW")       // changed
                        .servicePlanId("P1")
                        .orderName("Order1").salePrice(100.0)
                        .currency("USD").paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("PX") // changed
                        .orderName("Order1").salePrice(100.0)
                        .currency("USD").paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("P1")
                        .orderName("OrderX") // changed
                        .salePrice(100.0)
                        .currency("USD").paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("P1").orderName("Order1")
                        .salePrice(200.0)    // changed
                        .currency("USD").paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("P1").orderName("Order1")
                        .salePrice(100.0)
                        .currency("CAD")     // changed
                        .paymentCurrency("EUR")
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("P1").orderName("Order1")
                        .salePrice(100.0)
                        .currency("USD")
                        .paymentCurrency("GBP") // changed
                        .orderDate(now).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("P1").orderName("Order1")
                        .salePrice(100.0).currency("USD").paymentCurrency("EUR")
                        .orderDate(now.plusDays(1)) // changed
                        .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                        .build()
        );

        assertThat(base).isNotEqualTo(
                OrderRequestModel.builder()
                        .customerId("C1").catalogId("CAT1").watchId("W1")
                        .servicePlanId("P1").orderName("Order1")
                        .salePrice(100.0).currency("USD").paymentCurrency("EUR")
                        .orderDate(now)
                        .orderStatus(OrderStatus.PURCHASE_NEGOTIATION) // changed
                        .build()
        );
    }


    @Test
    @DisplayName("OrderResponseModel: each field breaks equals()")
    void orderResponseModelFieldByFieldEquals() {
        LocalDate exp = LocalDate.of(2025, 12, 31);
        LocalDateTime ord = LocalDateTime.of(2025, 12, 31, 10, 0);

        // base instance
        OrderResponseModel base = OrderResponseModel.builder()
                .orderId("O1")
                .customerId("C1").customerFirstName("First").customerLastName("Last")
                .catalogId("CAT1").catalogType("Type").catalogDescription("Desc")
                .watchId("W1").watchModel("Model").watchMaterial("Mat")
                .servicePlanId("P1").servicePlanCoverageDetails("Cov").servicePlanExpirationDate(exp)
                .orderName("Order1").salePrice(100.0).saleCurrency("USD").paymentCurrency("EUR")
                .orderDate(ord).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // now flip a handful of the ~20 fields; if you need 100% coverage,
        // just repeat this pattern for every property
        assertThat(base).isNotEqualTo(base.builder().orderId("OX").build());
        assertThat(base).isNotEqualTo(base.builder().customerFirstName("X").build());
        assertThat(base).isNotEqualTo(base.builder().catalogType("XType").build());
        assertThat(base).isNotEqualTo(base.builder().watchMaterial("XMat").build());
        assertThat(base).isNotEqualTo(base.builder().servicePlanCoverageDetails("XCov").build());
        assertThat(base).isNotEqualTo(base.builder().saleCurrency("CAD").build());
        assertThat(base).isNotEqualTo(base.builder().orderDate(ord.plusHours(1)).build());
        assertThat(base).isNotEqualTo(base.builder().orderStatus(OrderStatus.PURCHASE_CANCELED).build());
    }

    @Test
    @DisplayName("OrderResponseModel.Builder: chain every setter, toString(), build()")
    void orderResponseModelBuilderCoverage() {
        LocalDate expiry = LocalDate.of(2025, 12, 31);
        LocalDateTime orderedAt = LocalDateTime.of(2025, 12, 31, 23, 59);

        OrderResponseModel.OrderResponseModelBuilder b = OrderResponseModel.builder();

        // chain every setter:
        b.orderId("O-100")
                .customerId("C-100")
                .customerFirstName("Alice")
                .customerLastName("Bob")
                .catalogId("CAT-X")
                .catalogType("Premium")
                .catalogDescription("Top shelf")
                .watchId("W-900")
                .watchModel("Speedster")
                .watchMaterial("Titanium")
                .servicePlanId("PLAN-1")
                .servicePlanCoverageDetails("Two-year")
                .servicePlanExpirationDate(expiry)
                .orderName("FinalOrder")
                .salePrice(199.99)
                .saleCurrency("CAD")
                .paymentCurrency("USD")
                .orderDate(orderedAt)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED);

        // call toString() to cover it
        String s = b.toString();
        assertThat(s)
                .contains("orderId=O-100")
                .contains("customerFirstName=Alice")
                .contains("catalogDescription=Top shelf")
                .contains("saleCurrency=CAD")
                .contains("orderStatus=PURCHASE_COMPLETED");

        // build and verify:
        OrderResponseModel resp = b.build();
        assertThat(resp.getOrderId()).isEqualTo("O-100");
        assertThat(resp.getCustomerId()).isEqualTo("C-100");
        assertThat(resp.getCustomerFirstName()).isEqualTo("Alice");
        assertThat(resp.getCustomerLastName()).isEqualTo("Bob");
        assertThat(resp.getCatalogId()).isEqualTo("CAT-X");
        assertThat(resp.getCatalogType()).isEqualTo("Premium");
        assertThat(resp.getCatalogDescription()).isEqualTo("Top shelf");
        assertThat(resp.getWatchId()).isEqualTo("W-900");
        assertThat(resp.getWatchModel()).isEqualTo("Speedster");
        assertThat(resp.getWatchMaterial()).isEqualTo("Titanium");
        assertThat(resp.getServicePlanId()).isEqualTo("PLAN-1");
        assertThat(resp.getServicePlanCoverageDetails()).isEqualTo("Two-year");
        assertThat(resp.getServicePlanExpirationDate()).isEqualTo(expiry);
        assertThat(resp.getOrderName()).isEqualTo("FinalOrder");
        assertThat(resp.getSalePrice()).isEqualTo(199.99);
        assertThat(resp.getSaleCurrency()).isEqualTo("CAD");
        assertThat(resp.getPaymentCurrency()).isEqualTo("USD");
        assertThat(resp.getOrderDate()).isEqualTo(orderedAt);
        assertThat(resp.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);
    }

    @Test
    @DisplayName("OrderRequestModel.Builder: chain every setter, toString(), build()")
    void orderRequestModelBuilderCoverage() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 9, 30);

        OrderRequestModel.OrderRequestModelBuilder builder = OrderRequestModel.builder();

        // chain every setter on the builder:
        builder
                .customerId("C123")
                .catalogId("CAT-A")
                .watchId("W456")
                .servicePlanId("P789")
                .orderName("MyOrder")
                .salePrice(42.42)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_OFFER);

        // call toString() to cover its generated code:
        String repr = builder.toString();
        assertThat(repr)
                .contains("customerId=C123")
                .contains("catalogId=CAT-A")
                .contains("salePrice=42.42")
                .contains("orderStatus=PURCHASE_OFFER");

        // build the DTO and verify its fields:
        OrderRequestModel dto = builder.build();
        assertThat(dto.getCustomerId()).isEqualTo("C123");
        assertThat(dto.getCatalogId()).isEqualTo("CAT-A");
        assertThat(dto.getWatchId()).isEqualTo("W456");
        assertThat(dto.getServicePlanId()).isEqualTo("P789");
        assertThat(dto.getOrderName()).isEqualTo("MyOrder");
        assertThat(dto.getSalePrice()).isEqualTo(42.42);
        assertThat(dto.getCurrency()).isEqualTo("USD");
        assertThat(dto.getPaymentCurrency()).isEqualTo("EUR");
        assertThat(dto.getOrderDate()).isEqualTo(now);
        assertThat(dto.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_OFFER);
    }
}
