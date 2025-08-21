package com.example.apigatewayservice.presentationlayer;


import com.example.apigatewayservice.presentationlayer.orderdtos.Currency;


import com.example.apigatewayservice.presentationlayer.orderdtos.OrderRequestModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderResponseModel;
import com.example.apigatewayservice.presentationlayer.orderdtos.OrderStatus;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanRequestModel;
import com.example.apigatewayservice.presentationlayer.servicePlandtos.ServicePlanResponseModel;
import com.example.apigatewayservice.presentationlayer.watchdtos.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiControllerUnitTest {

    @Test
    void servicePlanRequestModelBuilder_chainToStringBuild() {
        // exercise every builder method
        LocalDate exp = LocalDate.of(2025, 12, 31);
        ServicePlanRequestModel.ServicePlanRequestModelBuilder builder =
                ServicePlanRequestModel.builder()
                        .coverageDetails("Basic Coverage")
                        .expirationDate(exp);

        // toString() on the builder should include both fields
        String repr = builder.toString();
        assertThat(repr)
                .contains("coverageDetails=Basic Coverage")
                .contains("expirationDate=" + exp);  // builder includes the date :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}

        // build & getters
        ServicePlanRequestModel req = builder.build();
        assertThat(req.getCoverageDetails()).isEqualTo("Basic Coverage");
        assertThat(req.getExpirationDate()).isEqualTo(exp);
    }

    @Test
    void servicePlanResponseModelBuilder_chainToStringBuildAndSetters() {
        // exercise builder
        LocalDate exp = LocalDate.of(2026, 6, 30);
        ServicePlanResponseModel.ServicePlanResponseModelBuilder builder =
                ServicePlanResponseModel.builder()
                        .planId("P100")
                        .coverageDetails("Premium")
                        .expirationDate(exp);

        // toString() on the builder should include all three fields
        String repr = builder.toString();
        assertThat(repr)
                .contains("planId=P100")
                .contains("coverageDetails=Premium")
                .contains("expirationDate=" + exp);  // builder includes the date :contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}

        // build & getters
        ServicePlanResponseModel resp = builder.build();
        assertThat(resp.getPlanId()).isEqualTo("P100");
        assertThat(resp.getCoverageDetails()).isEqualTo("Premium");
        assertThat(resp.getExpirationDate()).isEqualTo(exp);

        // setters
        resp.setCoverageDetails("Ultra");
        assertThat(resp.getCoverageDetails()).isEqualTo("Ultra");

    }

    @Test
    void watchRequestModelBuilder_chainToStringBuild() {
        // exercise every builder method on WatchRequestModel
        Accessory acc   = new Accessory("Strap", new BigDecimal("15.00"));
        Price     price = new Price(new BigDecimal("200"), new BigDecimal("150"), new BigDecimal("20"));
        WatchBrand brand = new WatchBrand("Omega", "CH");

        // <-- use the generated nested builder class name -->
        WatchRequestModel.WatchRequestModelBuilder builder =
                WatchRequestModel.builder()
                        .catalogId("CAT1")
                        .quantity(3)
                        .usageType(UsageType.USED)
                        .model("Speedmaster")
                        .material("Steel")
                        .accessories(List.of(acc))
                        .price(price)
                        .watchBrand(brand);

        // toString() on the builder should include all fields
        String repr = builder.toString();
        assertThat(repr)
                .contains("catalogId=CAT1")
                .contains("quantity=3")
                .contains("usageType=USED")
                .contains("model=Speedmaster")
                .contains("material=Steel");

        // build & verify getters
        WatchRequestModel req = builder.build();
        assertThat(req.getCatalogId()).isEqualTo("CAT1");
        assertThat(req.getQuantity()).isEqualTo(3);
        assertThat(req.getUsageType()).isEqualTo(UsageType.USED);
        assertThat(req.getModel()).isEqualTo("Speedmaster");
        assertThat(req.getMaterial()).isEqualTo("Steel");
        assertThat(req.getAccessories()).containsExactly(acc);
        assertThat(req.getPrice()).isEqualTo(price);
        assertThat(req.getWatchBrand()).isEqualTo(brand);
    }

    @Test
    void watchResponseModelBuilder_chainToStringBuildAndSetters() {
        // exercise every builder method on WatchResponseModel
        Accessory acc   = new Accessory("Bezel", new BigDecimal("30.00"));
        Price     price = new Price(new BigDecimal("500"), new BigDecimal("400"), new BigDecimal("50"));
        WatchBrand brand = new WatchBrand("Rolex", "CH");

        // <-- use the generated nested builder class name -->
        WatchResponseModel.WatchResponseModelBuilder builder =
                WatchResponseModel.builder()
                        .watchId("W123")
                        .catalogId("CAT2")
                        .quantity(10)
                        .usageType(UsageType.NEW)
                        .model("Submariner")
                        .material("Gold")
                        .accessories(List.of(acc))
                        .price(price)
                        .watchBrand(brand);

        // toString() on the builder should include all fields
        String repr = builder.toString();
        assertThat(repr)
                .contains("watchId=W123")
                .contains("catalogId=CAT2")
                .contains("quantity=10")
                .contains("usageType=NEW")
                .contains("model=Submariner")
                .contains("material=Gold");

        // build & verify getters
        WatchResponseModel resp = builder.build();
        assertThat(resp.getWatchId()).isEqualTo("W123");
        assertThat(resp.getCatalogId()).isEqualTo("CAT2");
        assertThat(resp.getQuantity()).isEqualTo(10);
        assertThat(resp.getUsageType()).isEqualTo(UsageType.NEW);
        assertThat(resp.getModel()).isEqualTo("Submariner");
        assertThat(resp.getMaterial()).isEqualTo("Gold");
        assertThat(resp.getAccessories()).containsExactly(acc);
        assertThat(resp.getPrice()).isEqualTo(price);
        assertThat(resp.getWatchBrand()).isEqualTo(brand);

        // exercise setters
        resp.setQuantity(20);
        resp.setMaterial("Titanium");
        assertThat(resp.getQuantity()).isEqualTo(20);
        assertThat(resp.getMaterial()).isEqualTo("Titanium");
    }


    @Test
    void price_getters_toString_equals() {
        Price p1 = new Price(new BigDecimal("100"), new BigDecimal("90"), new BigDecimal("10"));
        Price p2 = new Price(new BigDecimal("100"), new BigDecimal("90"), new BigDecimal("10"));
        Price p3 = new Price(new BigDecimal("200"), new BigDecimal("180"), new BigDecimal("20"));


        assertThat(p1.getMsrp()).isEqualByComparingTo("100");
        assertThat(p1.getCost()).isEqualByComparingTo("90");
        assertThat(p1.getTotalOptionsCost()).isEqualByComparingTo("10");

        // toString contains the actual field names
        String s = p1.toString();
        assertThat(s)
                .contains("msrp=100")
                .contains("cost=90")
                .contains("totalOptionsCost=10");

        // equals & hashCode
        assertThat(p1).isEqualTo(p2);
        assertThat(p1).hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(p3);
    }

    @Test
    void accessory_getters_setters_toString() {
        Accessory a = new Accessory("Strap", new BigDecimal("15.00"));
        // getters :contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}
        assertThat(a.getAccessoryName()).isEqualTo("Strap");
        assertThat(a.getAccessoryCost()).isEqualByComparingTo("15.00");

        // toString uses field names accessoryName & accessoryCost
        String s = a.toString();
        assertThat(s)
                .contains("accessoryName=Strap")
                .contains("accessoryCost=15.00");

        // setters
        a.setAccessoryName("Bezel");
        a.setAccessoryCost(new BigDecimal("30.00"));
        assertThat(a.getAccessoryName()).isEqualTo("Bezel");
        assertThat(a.getAccessoryCost()).isEqualByComparingTo("30.00");
    }

    @Test
    void watchBrand_getters_equals_toString() {
        WatchBrand b1 = new WatchBrand("Omega", "CH");
        WatchBrand b2 = new WatchBrand("Omega", "CH");
        WatchBrand b3 = new WatchBrand("Rolex", "CH");

        // getters :contentReference[oaicite:4]{index=4}:contentReference[oaicite:5]{index=5}
        assertThat(b1.getBrandName()).isEqualTo("Omega");
        assertThat(b1.getBrandCountry()).isEqualTo("CH");

        // toString uses brandName & brandCountry
        String s = b1.toString();
        assertThat(s)
                .contains("brandName=Omega")
                .contains("brandCountry=CH");

        // equals & hashCode
        assertThat(b1).isEqualTo(b2);
        assertThat(b1).hasSameHashCodeAs(b2);
        assertThat(b1).isNotEqualTo(b3);
    }

    @Test
    void watchStatus_enum_values() {
        // ensure all constants exist :contentReference[oaicite:6]{index=6}:contentReference[oaicite:7]{index=7}
        assertThat(WatchStatus.values())
                .containsExactlyInAnyOrder(
                        WatchStatus.AVAILABLE,
                        WatchStatus.SALE_PENDING,
                        WatchStatus.SOLD_OUT
                );

        // valueOf must use the exact names
        assertThat(WatchStatus.valueOf("AVAILABLE")).isEqualTo(WatchStatus.AVAILABLE);
        assertThat(WatchStatus.valueOf("SALE_PENDING")).isEqualTo(WatchStatus.SALE_PENDING);
        assertThat(WatchStatus.valueOf("SOLD_OUT")).isEqualTo(WatchStatus.SOLD_OUT);
    }

    @Test
    void defaultConstructor_and_gettersSetters() {
        // verify no-arg ctor + setters
        Accessory a = new Accessory();
        a.setAccessoryName("Strap");
        a.setAccessoryCost(new BigDecimal("15.00"));

        assertThat(a.getAccessoryName()).isEqualTo("Strap");
        assertThat(a.getAccessoryCost()).isEqualByComparingTo("15.00");
    }

    @Test
    void equals_and_hashCode_positiveAndNegative() {
        Accessory a1 = new Accessory("Bezel", new BigDecimal("30.00"));
        Accessory a2 = new Accessory("Bezel", new BigDecimal("30.00"));
        Accessory a3 = new Accessory("Clasp", new BigDecimal("5.00"));

        // reflexive
        assertThat(a1).isEqualTo(a1);
        // symmetric & consistent
        assertThat(a1).isEqualTo(a2);
        assertThat(a2).isEqualTo(a1);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());

        // negative cases
        assertThat(a1).isNotEqualTo(a3);
        assertThat(a1).isNotEqualTo(null);
        assertThat(a1).isNotEqualTo("not an accessory");
    }

    @Test
    void canEqual_behaviour() {
        Accessory a = new Accessory("Link", new BigDecimal("7.50"));
        Accessory same = new Accessory("Link", new BigDecimal("7.50"));

        // should be able to equal another Accessory
        assertThat(a.equals(same)).isTrue();

        // but not arbitrary object
        assertThat(a.equals("foo")).isFalse();
    }

    @Test
    void toString_containsAllFields() {
        Accessory a = new Accessory("Strap", new BigDecimal("15.00"));
        String s = a.toString();

        assertThat(s)
                .contains("accessoryName=Strap")
                .contains("accessoryCost=15.00");
    }


    @Test
    void orderRequestModelBuilder_chainToStringBuild() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
        OrderRequestModel req = OrderRequestModel.builder()
                .customerId("C1")
                .catalogId("CAT1")
                .watchId("W1")
                .servicePlanId("P1")
                .orderName("TestOrder")
                .salePrice(123.45)
                .currency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // toString()
        String repr = req.toString();
        assertThat(repr)
                .contains("customerId=C1")
                .contains("catalogId=CAT1")
                .contains("watchId=W1")
                .contains("orderName=TestOrder")
                .contains("salePrice=123.45")
                .contains("currency=USD")
                .contains("orderStatus=PURCHASE_COMPLETED");

        // getters
        assertThat(req.getCustomerId()).isEqualTo("C1");
        assertThat(req.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);
    }

    @Test
    void orderResponseModelBuilder_chainToStringBuildAndSetters() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDate exp = LocalDate.of(2026, 1, 1);
        OrderResponseModel resp = OrderResponseModel.builder()
                .orderId("O1")
                .customerId("C1")
                .customerFirstName("Alice")
                .customerLastName("Smith")
                .catalogId("CAT1")
                .catalogType("Standard")
                .catalogDescription("Everyday watches")
                .watchId("W1")
                .watchModel("X123")
                .watchMaterial("Steel")
                .servicePlanId("P1")
                .servicePlanCoverageDetails("2-year")
                .servicePlanExpirationDate(exp)
                .orderName("TestOrder")
                .salePrice(100.0)
                .saleCurrency("USD")
                .paymentCurrency("EUR")
                .orderDate(now)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        // toString()
        String repr = resp.toString();
        assertThat(repr)
                .contains("orderId=O1")
                .contains("customerId=C1")
                .contains("catalogId=CAT1")
                .contains("watchId=W1")
                .contains("orderName=TestOrder")
                .contains("salePrice=100.0")
                .contains("orderStatus=PURCHASE_COMPLETED");

        // getters
        assertThat(resp.getOrderId()).isEqualTo("O1");
        assertThat(resp.getServicePlanExpirationDate()).isEqualTo(exp);

        // setters
        resp.setOrderName("NewName");
        assertThat(resp.getOrderName()).isEqualTo("NewName");
    }

    @Test
    void price_builder_toString_equals_hashCode() {
        // fully qualified order‐DTO Price & Currency so you can keep the watch‐DTO import at the top
        com.example.apigatewayservice.presentationlayer.orderdtos.Price p1 =
                com.example.apigatewayservice.presentationlayer.orderdtos.Price.builder()
                        .amount(new BigDecimal("50"))
                        .currency(com.example.apigatewayservice.presentationlayer.orderdtos.Currency.USD)
                        .paymentCurrency(com.example.apigatewayservice.presentationlayer.orderdtos.Currency.CAD)
                        .build();

        com.example.apigatewayservice.presentationlayer.orderdtos.Price p2 =
                com.example.apigatewayservice.presentationlayer.orderdtos.Price.builder()
                        .amount(new BigDecimal("50"))
                        .currency(com.example.apigatewayservice.presentationlayer.orderdtos.Currency.USD)
                        .paymentCurrency(com.example.apigatewayservice.presentationlayer.orderdtos.Currency.CAD)
                        .build();

        com.example.apigatewayservice.presentationlayer.orderdtos.Price p3 =
                com.example.apigatewayservice.presentationlayer.orderdtos.Price.builder()
                        .amount(new BigDecimal("60"))
                        .currency(com.example.apigatewayservice.presentationlayer.orderdtos.Currency.USD)
                        .paymentCurrency(com.example.apigatewayservice.presentationlayer.orderdtos.Currency.CAD)
                        .build();

        // getters
        assertThat(p1.getAmount()).isEqualByComparingTo("50");
        assertThat(p1.getCurrency()).isEqualTo(
                com.example.apigatewayservice.presentationlayer.orderdtos.Currency.USD);
        assertThat(p1.getPaymentCurrency()).isEqualTo(
                com.example.apigatewayservice.presentationlayer.orderdtos.Currency.CAD);

        // toString()
        String s = p1.toString();
        assertThat(s)
                .contains("amount=50")
                .contains("currency=USD")
                .contains("paymentCurrency=CAD");

        // equals & hashCode
        assertThat(p1).isEqualTo(p2).hasSameHashCodeAs(p2);
        assertThat(p1).isNotEqualTo(p3);
    }
    @Test
    void currency_enum_values() {
        assertThat(Currency.values())
                .containsExactlyInAnyOrder(Currency.CAD, Currency.USD, Currency.SAR, Currency.EUR);
    }

    @Test
    void orderStatus_enum_values() {
        assertThat(OrderStatus.values())
                .containsExactlyInAnyOrder(
                        OrderStatus.PURCHASE_OFFER,
                        OrderStatus.PURCHASE_NEGOTIATION,
                        OrderStatus.PURCHASE_COMPLETED,
                        OrderStatus.PURCHASE_CANCELED
                );
    }

    @Test
    void orderRequestModelBuilder_chainToStringBuild_and_equalsHashCode() {
        LocalDateTime now = LocalDateTime.of(2025,1,1,12,0);

        // a) chain every setter on the builder
        OrderRequestModel.OrderRequestModelBuilder reqBuilder =
                OrderRequestModel.builder()
                        .customerId("C1")
                        .catalogId("CAT1")
                        .watchId("W1")
                        .servicePlanId("P1")
                        .orderName("MyOrder")
                        .salePrice(42.5)
                        .currency("USD")
                        .paymentCurrency("EUR")
                        .orderDate(now)
                        .orderStatus(OrderStatus.PURCHASE_OFFER);

        // b) toString() must mention each field
        String repr = reqBuilder.toString();
        assertThat(repr)
                .contains("customerId=C1")
                .contains("catalogId=CAT1")
                .contains("watchId=W1")
                .contains("servicePlanId=P1")
                .contains("orderName=MyOrder")
                .contains("salePrice=42.5")
                .contains("currency=USD")
                .contains("paymentCurrency=EUR")
                .contains("orderDate=" + now)
                .contains("orderStatus=PURCHASE_OFFER");

        // c) build & getters
        OrderRequestModel req = reqBuilder.build();
        assertThat(req.getCustomerId()).isEqualTo("C1");
        assertThat(req.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_OFFER);

        // d) setters
        req.setOrderName("NewName");
        assertThat(req.getOrderName()).isEqualTo("NewName");

        // e) equals & hashCode
        OrderRequestModel same = OrderRequestModel.builder()
                .customerId("C1").catalogId("CAT1").watchId("W1")
                .servicePlanId("P1").orderName("NewName").salePrice(42.5)
                .currency("USD").paymentCurrency("EUR")
                .orderDate(now).orderStatus(OrderStatus.PURCHASE_OFFER)
                .build();

        assertThat(req).isEqualTo(same).hasSameHashCodeAs(same);
        // negative
        OrderRequestModel diff = OrderRequestModel.builder()
                .customerId("C2") /* different */
                .catalogId("CAT1").watchId("W1")
                .servicePlanId("P1").orderName("NewName").salePrice(42.5)
                .currency("USD").paymentCurrency("EUR")
                .orderDate(now).orderStatus(OrderStatus.PURCHASE_OFFER)
                .build();
        assertThat(req).isNotEqualTo(diff);
        assertThat(req.equals("not a DTO")).isFalse();
        assertThat(req.equals(null)).isFalse();
    }

    @Test
    void orderResponseModelBuilder_chainToStringBuild_and_equalsHashCode() {
        LocalDateTime nowDateTime = LocalDateTime.of(2025,1,1,12,0);

        // change exp to LocalDate
        LocalDate exp = LocalDate.of(2026,1,1);

        // a) chain every setter on the builder
        OrderResponseModel.OrderResponseModelBuilder rspBuilder =
                OrderResponseModel.builder()
                        .orderId("O1")
                        .customerId("C1").customerFirstName("Alice").customerLastName("Smith")
                        .catalogId("CAT1").catalogType("Standard").catalogDescription("Everyday")
                        .watchId("W1").watchModel("X123").watchMaterial("Steel")
                        .servicePlanId("P1").servicePlanCoverageDetails("2-year").servicePlanExpirationDate(exp)
                        .orderName("Test")
                        .salePrice(99.9).saleCurrency("USD").paymentCurrency("EUR")
                        .orderDate(nowDateTime).orderStatus(OrderStatus.PURCHASE_COMPLETED);

        // b) toString() must mention all fields
        String repr = rspBuilder.toString();
        assertThat(repr)
                .contains("orderId=O1")
                .contains("customerId=C1")
                .contains("catalogId=CAT1")
                .contains("watchId=W1")
                .contains("servicePlanId=P1")
                .contains("orderName=Test")
                .contains("salePrice=99.9")
                .contains("saleCurrency=USD")
                .contains("paymentCurrency=EUR")
                .contains("orderDate=" + nowDateTime)
                .contains("orderStatus=PURCHASE_COMPLETED");

        // c) build & getters
        OrderResponseModel resp = rspBuilder.build();
        assertThat(resp.getOrderId()).isEqualTo("O1");
        assertThat(resp.getServicePlanExpirationDate()).isEqualTo(exp);

        // d) setters
        resp.setCatalogDescription("NewDesc");
        assertThat(resp.getCatalogDescription()).isEqualTo("NewDesc");

        // e) equals & hashCode
        OrderResponseModel same = OrderResponseModel.builder()
                .orderId("O1")
                .customerId("C1").customerFirstName("Alice").customerLastName("Smith")
                .catalogId("CAT1").catalogType("Standard").catalogDescription("NewDesc")
                .watchId("W1").watchModel("X123").watchMaterial("Steel")
                .servicePlanId("P1").servicePlanCoverageDetails("2-year").servicePlanExpirationDate(exp)
                .orderName("Test")
                .salePrice(99.9).saleCurrency("USD").paymentCurrency("EUR")
                .orderDate(nowDateTime).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();

        assertThat(resp).isEqualTo(same).hasSameHashCodeAs(same);
        OrderResponseModel diff = OrderResponseModel.builder()
                .orderId("O2")  // different ID
                // ... copy other fields from same ...
                .customerId("C1").customerFirstName("Alice").customerLastName("Smith")
                .catalogId("CAT1").catalogType("Standard").catalogDescription("NewDesc")
                .watchId("W1").watchModel("X123").watchMaterial("Steel")
                .servicePlanId("P1").servicePlanCoverageDetails("2-year").servicePlanExpirationDate(exp)
                .orderName("Test")
                .salePrice(99.9).saleCurrency("USD").paymentCurrency("EUR")
                .orderDate(nowDateTime).orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .build();
        assertThat(resp).isNotEqualTo(diff);
        assertThat(resp.equals("foo")).isFalse();
        assertThat(resp.equals(null)).isFalse();
    }

    @Test
    void orderRequestModel_setters_equals_hashCode() {
        LocalDateTime now = LocalDateTime.of(2025,1,1,12,0);

        // use the all-args constructor to get a fully populated instance
        OrderRequestModel r1 = new OrderRequestModel(
                "C1", "CAT1", "W1", "P1",
                "MyOrder", 42.5, "USD", "EUR",
                now, OrderStatus.PURCHASE_OFFER
        );

        // exercise every setter
        r1.setCustomerId("C2");
        r1.setCatalogId("CAT2");
        r1.setWatchId("W2");
        r1.setServicePlanId("P2");
        r1.setOrderName("OtherOrder");
        r1.setSalePrice(99.9);
        r1.setCurrency("CAD");
        r1.setPaymentCurrency("GBP");
        LocalDateTime later = now.plusDays(1);
        r1.setOrderDate(later);
        r1.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);

        // verify via getters
        assertThat(r1.getCustomerId()).isEqualTo("C2");
        assertThat(r1.getCatalogId()).isEqualTo("CAT2");
        assertThat(r1.getWatchId()).isEqualTo("W2");
        assertThat(r1.getServicePlanId()).isEqualTo("P2");
        assertThat(r1.getOrderName()).isEqualTo("OtherOrder");
        assertThat(r1.getSalePrice()).isEqualTo(99.9);
        assertThat(r1.getCurrency()).isEqualTo("CAD");
        assertThat(r1.getPaymentCurrency()).isEqualTo("GBP");
        assertThat(r1.getOrderDate()).isEqualTo(later);
        assertThat(r1.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_COMPLETED);

        // equals & hashCode: build a second object with same fields
        OrderRequestModel r2 = new OrderRequestModel(
                "C2", "CAT2", "W2", "P2",
                "OtherOrder", 99.9, "CAD", "GBP",
                later, OrderStatus.PURCHASE_COMPLETED
        );
        assertThat(r1).isEqualTo(r2).hasSameHashCodeAs(r2);
        // negative cases
        assertThat(r1).isNotEqualTo(r2.builder().customerId("X").build());
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1).isNotEqualTo("not a model");
    }

    @Test
    void orderResponseModel_setters_equals_hashCode() {
        LocalDateTime now = LocalDateTime.of(2025,1,1,12,0);
        LocalDateTime exp  = LocalDateTime.of(2026,1,1,0,0);

        // all-args constructor
        OrderResponseModel o1 = new OrderResponseModel(
                "O1",
                "C1", "Alice", "Smith",
                "CAT1", "Standard", "Everyday",
                "W1", "X123", "Steel",
                "P1", "2-year", exp.toLocalDate(),
                "Test", 55.5, "USD", "EUR",
                now, OrderStatus.PURCHASE_COMPLETED
        );

        // exercise setters
        o1.setOrderId("O2");
        o1.setCustomerFirstName("Bob");
        o1.setCustomerLastName("Jones");
        o1.setCatalogDescription("Luxury");
        o1.setWatchMaterial("Gold");
        o1.setServicePlanExpirationDate(exp.toLocalDate().plusYears(1));
        o1.setOrderName("NewTest");
        o1.setSalePrice(77.7);
        o1.setSaleCurrency("CAD");
        o1.setPaymentCurrency("GBP");
        LocalDateTime later = now.plusDays(2);
        o1.setOrderDate(later);
        o1.setOrderStatus(OrderStatus.PURCHASE_CANCELED);

        // verify via getters
        assertThat(o1.getOrderId()).isEqualTo("O2");
        assertThat(o1.getCustomerFirstName()).isEqualTo("Bob");
        assertThat(o1.getCustomerLastName()).isEqualTo("Jones");
        assertThat(o1.getCatalogDescription()).isEqualTo("Luxury");
        assertThat(o1.getWatchMaterial()).isEqualTo("Gold");
        assertThat(o1.getServicePlanExpirationDate()).isEqualTo(exp.toLocalDate().plusYears(1));
        assertThat(o1.getOrderName()).isEqualTo("NewTest");
        assertThat(o1.getSalePrice()).isEqualTo(77.7);
        assertThat(o1.getSaleCurrency()).isEqualTo("CAD");
        assertThat(o1.getPaymentCurrency()).isEqualTo("GBP");
        assertThat(o1.getOrderDate()).isEqualTo(later);
        assertThat(o1.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_CANCELED);

        // equals & hashCode: second object same fields
        OrderResponseModel o2 = new OrderResponseModel();
        // copy all via setters
        o2.setOrderId("O2");
        o2.setCustomerId("C1");
        o2.setCustomerFirstName("Bob");
        o2.setCustomerLastName("Jones");
        o2.setCatalogId("CAT1");
        o2.setCatalogType("Standard");
        o2.setCatalogDescription("Luxury");
        o2.setWatchId("W1");
        o2.setWatchModel("X123");
        o2.setWatchMaterial("Gold");
        o2.setServicePlanId("P1");
        o2.setServicePlanCoverageDetails("2-year");
        o2.setServicePlanExpirationDate(exp.toLocalDate().plusYears(1));
        o2.setOrderName("NewTest");
        o2.setSalePrice(77.7);
        o2.setSaleCurrency("CAD");
        o2.setPaymentCurrency("GBP");
        o2.setOrderDate(later);
        o2.setOrderStatus(OrderStatus.PURCHASE_CANCELED);

        assertThat(o1).isEqualTo(o2).hasSameHashCodeAs(o2);
        // negative
        o2.setOrderId("XXXX");
        assertThat(o1).isNotEqualTo(o2);
        assertThat(o1).isNotEqualTo(null);
        assertThat(o1).isNotEqualTo("foo");
    }
}
