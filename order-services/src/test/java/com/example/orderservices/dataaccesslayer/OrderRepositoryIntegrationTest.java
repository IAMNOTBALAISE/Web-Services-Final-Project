package com.example.orderservices.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@TestPropertySource(properties = {
        "spring.data.mongodb.port=0", // Use a random port
        "spring.mongodb.embedded.version=5.0.5",
        "de.flapdoodle.mongodb.embedded.version=5.0.5"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository repository;

    private Order completedOrder;
    private Order canceledOrder;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        // Positive: prepare two orders, one completed and one canceled
        completedOrder = Order.builder()
                .id("1")
                .orderIdentifier(new OrderIdentifier("OID-1"))
                .orderName("TestOrder1")
                .customerIdentifier(new CustomerIdentifier("CUST-1"))
                .catalogIdentifier(new CatalogIdentifier("CAT-1"))
                .watchIdentifier(new WatchIdentifier("WCH-1"))
                .servicePlanIdentifier(new ServicePlanIdentifier("SP-1"))
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .price(new Price(new BigDecimal("100.00"), Currency.USD, Currency.USD))
                .currency(Currency.USD)
                .orderDate(LocalDateTime.now())
                .build();
        canceledOrder = Order.builder()
                .id("2")
                .orderIdentifier(new OrderIdentifier("OID-2"))
                .orderName("TestOrder2")
                .customerIdentifier(new CustomerIdentifier("CUST-2"))
                .catalogIdentifier(new CatalogIdentifier("CAT-2"))
                .watchIdentifier(new WatchIdentifier("WCH-2"))
                .servicePlanIdentifier(new ServicePlanIdentifier("SP-2"))
                .orderStatus(OrderStatus.PURCHASE_CANCELED)
                .price(new Price(new BigDecimal("200.00"), Currency.CAD, Currency.CAD))
                .currency(Currency.CAD)
                .orderDate(LocalDateTime.now())
                .build();

        repository.saveAll(List.of(completedOrder, canceledOrder));
    }

    @Test
    @DisplayName("Positive: existsByOrderIdentifier returns true for existing orderId")
    void existsByOrderIdentifier_existing_returnsTrue() {
        assertThat(repository.existsByOrderIdentifier_OrderId(
                completedOrder.getOrderIdentifier().getOrderId()
        )).isTrue();
    }

    @Test
    @DisplayName("Negative: existsByOrderIdentifier returns false for missing orderId")
    void existsByOrderIdentifier_missing_returnsFalse() {
        assertThat(repository.existsByOrderIdentifier_OrderId("unknown")).isFalse();
    }

    @Test
    @DisplayName("Positive: findOrderByOrderIdentifier returns correct Order")
    void findByOrderIdentifier_existing_returnsOrder() {
        Order found = repository.findOrderByOrderIdentifier_OrderId(
                canceledOrder.getOrderIdentifier().getOrderId()
        );
        assertThat(found).isNotNull();
        assertThat(found.getOrderName()).isEqualTo("TestOrder2");
    }

    @Test
    @DisplayName("Negative: findOrderByOrderIdentifier returns null if missing")
    void findByOrderIdentifier_missing_returnsNull() {
        assertThat(repository.findOrderByOrderIdentifier_OrderId("no-id")).isNull();
    }

    @Test
    @DisplayName("Positive: existsByWatchIdentifier returns true")
    void existsByWatchIdentifier_existing_returnsTrue() {
        assertThat(repository.existsByWatchIdentifier_WatchId(
                completedOrder.getWatchIdentifier().getWatchId()
        )).isTrue();
    }

    @Test
    @DisplayName("Negative: existsByWatchIdentifier returns false")
    void existsByWatchIdentifier_missing_returnsFalse() {
        assertThat(repository.existsByWatchIdentifier_WatchId("no-watch")).isFalse();
    }

    @Test
    @DisplayName("Positive: existsByOrderName returns true for existing orderName")
    void existsByOrderName_existing_returnsTrue() {
        assertThat(repository.existsByOrderName("TestOrder1")).isTrue();
    }

    @Test
    @DisplayName("Negative: existsByOrderName returns false for missing orderName")
    void existsByOrderName_missing_returnsFalse() {
        assertThat(repository.existsByOrderName("Nonexistent")).isFalse();
    }

    @Test
    @DisplayName("Positive: existsByWatchIdentifierAndOrderStatus returns true when matching status")
    void existsByWatchAndStatus_matching_returnsTrue() {
        assertThat(repository.existsByWatchIdentifier_WatchIdAndOrderStatus(
                completedOrder.getWatchIdentifier().getWatchId(),
                OrderStatus.PURCHASE_COMPLETED
        )).isTrue();
    }

    @Test
    @DisplayName("Negative: existsByWatchIdentifierAndOrderStatus returns false when status does not match")
    void existsByWatchAndStatus_nonMatching_returnsFalse() {
        assertThat(repository.existsByWatchIdentifier_WatchIdAndOrderStatus(
                canceledOrder.getWatchIdentifier().getWatchId(),
                OrderStatus.PURCHASE_COMPLETED
        )).isFalse();
    }

    @Test
    @DisplayName("Positive: OrderIdentifier no-arg ctor generates non-null, unique IDs")
    void orderIdentifierNoArgCtor_generatesUniqueNonNull() {
        OrderIdentifier a = new OrderIdentifier();
        OrderIdentifier b = new OrderIdentifier();
        assertThat(a.getOrderId()).isNotNull().isNotEmpty();
        assertThat(b.getOrderId()).isNotNull().isNotEmpty();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("Positive: OrderIdentifier all-args ctor respects provided value")
    void orderIdentifierAllArgs_ctorSetsValue() {
        OrderIdentifier id = new OrderIdentifier("XYZ-123");
        assertThat(id.getOrderId()).isEqualTo("XYZ-123");
    }

    @Test
    @DisplayName("Positive: CustomerIdentifier no-arg ctor generates unique")
    void customerIdentifierNoArg_ctorGeneratesUnique() {
        CustomerIdentifier a = new CustomerIdentifier();
        CustomerIdentifier b = new CustomerIdentifier();
        assertThat(a.getCustomerId()).isNotNull().isNotEmpty();
        assertThat(b.getCustomerId()).isNotNull().isNotEmpty();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("Positive: CatalogIdentifier no-arg ctor generates unique")
    void catalogIdentifierNoArg_ctorGeneratesUnique() {
        CatalogIdentifier a = new CatalogIdentifier();
        CatalogIdentifier b = new CatalogIdentifier();
        assertThat(a.getCatalogId()).isNotNull().isNotEmpty();
        assertThat(b.getCatalogId()).isNotNull().isNotEmpty();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("Positive: ServicePlanIdentifier no-arg ctor generates unique")
    void servicePlanIdentifierNoArg_ctorGeneratesUnique() {
        ServicePlanIdentifier a = new ServicePlanIdentifier();
        ServicePlanIdentifier b = new ServicePlanIdentifier();
        assertThat(a.getPlanId()).isNotNull().isNotEmpty();
        assertThat(b.getPlanId()).isNotNull().isNotEmpty();
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("Positive: WatchIdentifier no-arg ctor generates unique")
    void watchIdentifierNoArg_ctorGeneratesUnique() {
        WatchIdentifier a = new WatchIdentifier();
        WatchIdentifier b = new WatchIdentifier();
        assertThat(a.getWatchId()).isNotNull().isNotEmpty();
        assertThat(b.getWatchId()).isNotNull().isNotEmpty();
        assertThat(a).isNotEqualTo(b);
    }

    //---- Price value object ----//

    @Test
    @DisplayName("Positive: Price builder and getters")
    void priceBuilder_andGetters() {
        Price p = Price.builder()
                .amount(new BigDecimal("19.99"))
                .currency(Currency.EUR)
                .paymentCurrency(Currency.USD)
                .build();
        assertThat(p.getAmount()).isEqualByComparingTo("19.99");
        assertThat(p.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(p.getPaymentCurrency()).isEqualTo(Currency.USD);
    }

    //---- Enums ----//

    @Test
    @DisplayName("Positive: OrderStatus contains all expected values")
    void orderStatus_enumValues() {
        Set<String> names = new HashSet<>();
        for (OrderStatus os : OrderStatus.values()) {
            names.add(os.name());
        }
        assertThat(names)
                .containsExactlyInAnyOrder("PURCHASE_OFFER",
                        "PURCHASE_NEGOTIATION",
                        "PURCHASE_COMPLETED",
                        "PURCHASE_CANCELED");
    }

    @Test
    @DisplayName("Positive: WatchStatus contains expected values")
    void watchStatus_enumValues() {
        assertThat(WatchStatus.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("AVAILABLE", "SALE_PENDING", "SOLD_OUT");
    }

    @Test
    @DisplayName("Positive: Currency enum has four entries")
    void currency_enumHasFour() {
        assertThat(Currency.values()).hasSize(4)
                .extracting(Enum::name)
                .containsExactlyInAnyOrder("CAD","USD","SAR","EUR");
    }

    //---- Order itself ----//

    @Test
    @DisplayName("Positive: Order builder / equals / hashCode")
    void orderBuilder_andEqualsHashCode() {
        Order o1 = Order.builder()
                .id("id")
                .orderIdentifier(new OrderIdentifier("OID"))
                .orderName("Name")
                .customerIdentifier(new CustomerIdentifier("C1"))
                .catalogIdentifier(new CatalogIdentifier("C2"))
                .watchIdentifier(new WatchIdentifier("W1"))
                .servicePlanIdentifier(new ServicePlanIdentifier("SP1"))
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .price(new Price(new BigDecimal("10.00"), Currency.SAR, Currency.SAR))
                .currency(Currency.SAR)
                .orderDate(java.time.LocalDateTime.now())
                .build();

        Order o2 = Order.builder()
                .id("id")
                .orderIdentifier(new OrderIdentifier("OID"))
                .orderName("Name")
                .customerIdentifier(new CustomerIdentifier("C1"))
                .catalogIdentifier(new CatalogIdentifier("C2"))
                .watchIdentifier(new WatchIdentifier("W1"))
                .servicePlanIdentifier(new ServicePlanIdentifier("SP1"))
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .price(new Price(new BigDecimal("10.00"), Currency.SAR, Currency.SAR))
                .currency(Currency.SAR)
                .orderDate(o1.getOrderDate())
                .build();

        assertThat(o1).isEqualTo(o2);
        assertThat(o1.hashCode()).isEqualTo(o2.hashCode());
    }

    @Test
    @DisplayName("Positive: No-arg Order + setters works and toString contains fields")
    void orderNoArgConstructor_andSetters_andToString() {
        Order order = new Order();
        order.setId("xyz");
        order.setOrderName("NameX");
        order.setOrderStatus(OrderStatus.PURCHASE_OFFER);

        String repr = order.toString();
        assertThat(repr)
                .contains("xyz")
                .contains("NameX")
                .contains("PURCHASE_OFFER");
    }

    @Test
    @DisplayName("Positive: equals reflexive, null and wrong-type false")
    void orderEquals_andCanEqual_edgeCases() {
        Order o = completedOrder;
        // reflexive
        assertThat(o).isEqualTo(o);
        // null
        assertThat(o.equals(null)).isFalse();
        // different type
        assertThat(o.equals("not an order")).isFalse();
    }

    @Test
    @DisplayName("Positive: hashCode is stable across calls")
    void orderHashCode_stability() {
        Order o = completedOrder;
        int first = o.hashCode();
        int second = o.hashCode();
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("OrderIdentifier: no-arg, setter, all-arg, toString, equals/hashCode")
    void orderIdentifier_allAccessors_andObjectMethods() {
        OrderIdentifier a = new OrderIdentifier();           // no-arg
        assertThat(a.getOrderId()).isNotNull().isNotEmpty();

        a.setOrderId("XID");
        assertThat(a.getOrderId()).isEqualTo("XID");
        assertThat(a.toString()).contains("XID");

        OrderIdentifier b = new OrderIdentifier("XID");
        assertThat(a).isEqualTo(b)
                .hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(new OrderIdentifier("YID"));
    }

    @Test
    @DisplayName("CustomerIdentifier: no-arg, setter, toString, equals/hashCode")
    void customerIdentifier_allAccessors_andObjectMethods() {
        CustomerIdentifier a = new CustomerIdentifier();
        assertThat(a.getCustomerId()).isNotNull().isNotEmpty();

        a.setCustomerId("C123");
        assertThat(a.getCustomerId()).isEqualTo("C123");
        assertThat(a.toString()).contains("C123");

        CustomerIdentifier b = new CustomerIdentifier("C123");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("CatalogIdentifier: no-arg, setter, toString, equals/hashCode")
    void catalogIdentifier_allAccessors_andObjectMethods() {
        CatalogIdentifier a = new CatalogIdentifier();
        assertThat(a.getCatalogId()).isNotNull().isNotEmpty();

        a.setCatalogId("CATX");
        assertThat(a.getCatalogId()).isEqualTo("CATX");
        assertThat(a.toString()).contains("CATX");

        CatalogIdentifier b = new CatalogIdentifier("CATX");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("WatchIdentifier: no-arg, setter, equals/hashCode")
    void watchIdentifier_allAccessors_andObjectMethods() {
        WatchIdentifier a = new WatchIdentifier();
        assertThat(a.getWatchId()).isNotNull().isNotEmpty();

        a.setWatchId("WCHX");
        assertThat(a.getWatchId()).isEqualTo("WCHX");

        WatchIdentifier b = new WatchIdentifier("WCHX");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("ServicePlanIdentifier: no-arg, setter, toString, equals/hashCode")
    void servicePlanIdentifier_allAccessors_andObjectMethods() {
        ServicePlanIdentifier a = new ServicePlanIdentifier();
        assertThat(a.getPlanId()).isNotNull().isNotEmpty();

        a.setPlanId("PLAN1");
        assertThat(a.getPlanId()).isEqualTo("PLAN1");


        ServicePlanIdentifier b = new ServicePlanIdentifier("PLAN1");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }

    //---- PRICE VALUE OBJECT ----//

    @Test
    @DisplayName("Price: no-arg/setters, builder, toString, equals/hashCode")
    void price_allAccessors_andObjectMethods() {
        // no-arg + setters
        Price p1 = new Price();
        p1.setAmount(new BigDecimal("5.00"));
        p1.setCurrency(Currency.EUR);
        p1.setPaymentCurrency(Currency.USD);

        assertThat(p1.getAmount()).isEqualByComparingTo("5.00");
        assertThat(p1.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(p1.getPaymentCurrency()).isEqualTo(Currency.USD);
        assertThat(p1.toString())
                .contains("5.00")
                .contains("EUR")
                .contains("USD");

        // builder + all-args
        Price p2 = Price.builder()
                .amount(new BigDecimal("5.00"))
                .currency(Currency.EUR)
                .paymentCurrency(Currency.USD)
                .build();

        assertThat(p2).isEqualTo(p1)
                .hasSameHashCodeAs(p1);
    }

    //---- ENUMS ----//

    @Test
    @DisplayName("Currency enum covers all 4 and has correct names")
    void currency_enumIntegrity() {
        String[] expected = { "CAD", "USD", "SAR", "EUR" };
        Set<String> names = new HashSet<>();
        for (Currency c : Currency.values()) {
            names.add(c.name());
            // toString() of an enum is its name by default
            assertThat(c.toString()).isEqualTo(c.name());
        }
        assertThat(names).containsExactlyInAnyOrder(expected);
        assertThat(Currency.valueOf("USD")).isEqualTo(Currency.USD);
    }

    @Test
    @DisplayName("OrderStatus enum covers all expected purchase phases")
    void orderStatus_enumIntegrity() {
        String[] expected = {
                "PURCHASE_OFFER",
                "PURCHASE_NEGOTIATION",
                "PURCHASE_COMPLETED",
                "PURCHASE_CANCELED"
        };
        Set<String> names = new HashSet<>();
        for (OrderStatus os : OrderStatus.values()) {
            names.add(os.name());
            assertThat(os.toString()).isEqualTo(os.name());
        }
        assertThat(names)
                .containsExactlyInAnyOrder(expected);
    }

    @Test
    @DisplayName("WatchStatus enum covers all states")
    void watchStatus_enumIntegrity() {
        String[] expected = {
                "AVAILABLE",
                "SALE_PENDING",
                "SOLD_OUT"
        };
        Set<String> names = new HashSet<>();
        for (WatchStatus ws : WatchStatus.values()) {
            names.add(ws.name());
            assertThat(ws.toString()).isEqualTo(ws.name());
        }
        assertThat(names)
                .containsExactlyInAnyOrder(expected);
    }

    //---- Order entity minimal smoke ----//

    @Test
    @DisplayName("Order: minimal no-arg + setter + toString")
    void order_minimalNoArgAndSetterAndToString() {
        Order o = new Order();
        o.setOrderName("Smoke");
        o.setOrderStatus(OrderStatus.PURCHASE_COMPLETED);
        String repr = o.toString();
        assertThat(repr)
                .contains("Smoke")
                .contains("PURCHASE_COMPLETED");
    }

    @Test
    @DisplayName("Comprehensive integrity: equals, hashCode and toString on all VO/IDs/Entity/Enums")
    void allObjectMethods_integrity() {
        // --- Identifiers ---
        OrderIdentifier oi1 = new OrderIdentifier("X");
        OrderIdentifier oi2 = new OrderIdentifier("X");
        assertThat(oi1).isEqualTo(oi2).hasSameHashCodeAs(oi2);
        assertThat(oi1.toString()).contains("X");
        // **exercise setter/getter**
        oi1.setOrderId("Y");
        assertThat(oi1.getOrderId()).isEqualTo("Y");

        CustomerIdentifier ci1 = new CustomerIdentifier("C");
        CustomerIdentifier ci2 = new CustomerIdentifier("C");
        assertThat(ci1).isEqualTo(ci2).hasSameHashCodeAs(ci2);
        assertThat(ci1.toString()).contains("C");
        ci1.setCustomerId("D");
        assertThat(ci1.getCustomerId()).isEqualTo("D");

        CatalogIdentifier cat1 = new CatalogIdentifier("K");
        CatalogIdentifier cat2 = new CatalogIdentifier("K");
        assertThat(cat1).isEqualTo(cat2).hasSameHashCodeAs(cat2);
        assertThat(cat1.toString()).contains("K");
        cat1.setCatalogId("L");
        assertThat(cat1.getCatalogId()).isEqualTo("L");

        WatchIdentifier wi1 = new WatchIdentifier("W");
        WatchIdentifier wi2 = new WatchIdentifier("W");
        assertThat(wi1).isEqualTo(wi2).hasSameHashCodeAs(wi2);
        assertThat(wi1.toString()).contains("W");
        wi1.setWatchId("Z");
        assertThat(wi1.getWatchId()).isEqualTo("Z");

        ServicePlanIdentifier sp1 = new ServicePlanIdentifier("P");
        ServicePlanIdentifier sp2 = new ServicePlanIdentifier("P");
        assertThat(sp1).isEqualTo(sp2).hasSameHashCodeAs(sp2);
        assertThat(sp1.toString()).contains("P");
        sp1.setPlanId("Q");
        assertThat(sp1.getPlanId()).isEqualTo("Q");

        // --- Price value‐object ---
        Price p1 = Price.builder()
                .amount(new BigDecimal("9.99"))
                .currency(Currency.EUR)
                .paymentCurrency(Currency.USD)
                .build();
        Price p2 = Price.builder()
                .amount(new BigDecimal("9.99"))
                .currency(Currency.EUR)
                .paymentCurrency(Currency.USD)
                .build();
        assertThat(p1).isEqualTo(p2).hasSameHashCodeAs(p2);
        assertThat(p1.toString()).contains("9.99", "EUR", "USD");

        // **no-arg + setters/getters**
        Price p3 = new Price();
        p3.setAmount(new BigDecimal("1.23"));
        p3.setCurrency(Currency.SAR);
        p3.setPaymentCurrency(Currency.CAD);
        assertThat(p3.getAmount()).isEqualByComparingTo("1.23");
        assertThat(p3.getCurrency()).isEqualTo(Currency.SAR);
        assertThat(p3.getPaymentCurrency()).isEqualTo(Currency.CAD);
        assertThat(p3.toString()).contains("1.23", "SAR", "CAD");

        // **builder.toString() coverage**
        String priceBuilderStr = Price.builder().toString();
        assertThat(priceBuilderStr).contains("PriceBuilder");

        // --- Enums ---
        for (Currency c : Currency.values()) {
            assertThat(c.toString()).isEqualTo(c.name());
        }
        for (OrderStatus s : OrderStatus.values()) {
            assertThat(s.toString()).isEqualTo(s.name());
        }
        for (WatchStatus s : WatchStatus.values()) {
            assertThat(s.toString()).isEqualTo(s.name());
        }

        // --- Order entity ---
        // builder/equal/hashCode
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Order o1 = Order.builder()
                .id("ID")
                .orderIdentifier(oi1)
                .orderName("N")
                .customerIdentifier(ci1)
                .catalogIdentifier(cat1)
                .watchIdentifier(wi1)
                .servicePlanIdentifier(sp1)
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .price(p1)
                .currency(Currency.EUR)
                .orderDate(now)
                .build();
        Order o2 = Order.builder()
                .id("ID")
                .orderIdentifier(new OrderIdentifier("Y"))
                .orderName("N")
                .customerIdentifier(new CustomerIdentifier("D"))
                .catalogIdentifier(new CatalogIdentifier("L"))
                .watchIdentifier(new WatchIdentifier("Z"))
                .servicePlanIdentifier(new ServicePlanIdentifier("Q"))
                .orderStatus(OrderStatus.PURCHASE_COMPLETED)
                .price(p2)
                .currency(Currency.EUR)
                .orderDate(now)
                .build();

        assertThat(o1).isEqualTo(o2).hasSameHashCodeAs(o2);

        // **toString contains everything**
        String orderStr = o1.toString();
        assertThat(orderStr)
                .contains("ID", "N", "PURCHASE_COMPLETED")
                .contains("Y", "D", "L", "Z", "Q")
                .contains("9.99", "EUR");

        // **no-arg + all setters/getters on Order**
        Order o3 = new Order();
        o3.setId("XID");
        o3.setOrderIdentifier(new OrderIdentifier("OX"));
        o3.setOrderName("XYZ");
        o3.setCustomerIdentifier(new CustomerIdentifier("CX"));
        o3.setCatalogIdentifier(new CatalogIdentifier("CLX"));
        o3.setWatchIdentifier(new WatchIdentifier("WX"));
        o3.setServicePlanIdentifier(new ServicePlanIdentifier("SX"));
        o3.setOrderStatus(OrderStatus.PURCHASE_NEGOTIATION);
        o3.setPrice(p3);
        o3.setCurrency(Currency.SAR);
        o3.setOrderDate(now);
        assertThat(o3.getId()).isEqualTo("XID");
        assertThat(o3.getOrderIdentifier().getOrderId()).isEqualTo("OX");
        assertThat(o3.getOrderName()).isEqualTo("XYZ");
        assertThat(o3.getCustomerIdentifier().getCustomerId()).isEqualTo("CX");
        assertThat(o3.getCatalogIdentifier().getCatalogId()).isEqualTo("CLX");
        assertThat(o3.getWatchIdentifier().getWatchId()).isEqualTo("WX");
        assertThat(o3.getServicePlanIdentifier().getPlanId()).isEqualTo("SX");
        assertThat(o3.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_NEGOTIATION);
        assertThat(o3.getPrice()).isEqualTo(p3);
        assertThat(o3.getCurrency()).isEqualTo(Currency.SAR);
        assertThat(o3.getOrderDate()).isEqualTo(now);

        // **OrderBuilder.toString() coverage**
        String orderBuilderStr = Order.builder().toString();
        assertThat(orderBuilderStr).contains("OrderBuilder");

        // === edge cases for equals ===
        assertThat(o1.equals(null)).isFalse();
        assertThat(o1.equals("foo")).isFalse();
        assertThat(ci1.equals(null)).isFalse();
        assertThat(ci1.equals(123)).isFalse();
        assertThat(p1.equals(p3)).isFalse();
    }
}