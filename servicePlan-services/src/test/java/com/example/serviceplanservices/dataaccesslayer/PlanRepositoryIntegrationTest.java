package com.example.serviceplanservices.dataaccesslayer;

import com.example.serviceplanservices.ServicePlanServicesApplication;
import com.example.serviceplanservices.utils.exceptions.DuplicateCoverageDetailsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PlanRepositoryIntegrationTest {

    @Autowired
    private ServicePlanRepository repo;

    private static final String SP1 = "SP-001";
    private static final String SP2 = "SP-002";



    // ─── Positive: lookup an existing plan by its business ID
    @Test
    void findByPlanId_existing_returnsPlan() {
        ServicePlan found = repo.findByServicePlanIdentifier_PlanId(SP1);
        assertNotNull(found, "SP-001 should have been seeded");
        assertEquals(SP1, found.getServicePlanIdentifier().getPlanId());
        assertEquals("Full coverage for 1 year", found.getCoverageDetails());
    }

    // ─── Negative: lookup by a non‑existent business ID returns null
    @Test
    void findByPlanId_missing_returnsNull() {

        assertNull(repo.findByServicePlanIdentifier_PlanId("NO-SUCH"));
    }

    // ─── Positive/Negative: existsByCoverageDetails
    @Test
    void existsByCoverageDetails_trueForSeed_falseOtherwise() {
        assertTrue(repo.existsByCoverageDetails("Full coverage for 1 year"));
        assertFalse(repo.existsByCoverageDetails("Some other coverage"));
    }

    // ─── Positive: findAll returns exactly the two that we seeded
    @Test
    void findAll_returnsTwoRows() {
        List<ServicePlan> all = repo.findAll();
        assertEquals(2, all.size());
    }

    // ─── Positive: deleting one plan actually removes it
    @Test
    void deletePlan_removesIt() {
        ServicePlan toDelete = repo.findByServicePlanIdentifier_PlanId(SP2);
        assertNotNull(toDelete);
        repo.delete(toDelete);
        assertNull(repo.findByServicePlanIdentifier_PlanId(SP2));
    }

    // ─── Negative: inserting a duplicate business ID violates the unique constraint
    @Test
    void saveDuplicatePlanId_throws() {
        ServicePlan dup = new ServicePlan();
        dup.setServicePlanIdentifier(new ServicePlanIdentifier(SP1));
        dup.setCoverageDetails("Another coverage");
        dup.setExpirationDate(LocalDate.now().plusDays(1));

        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> repo.saveAndFlush(dup),
                "Should not allow two plans with the same plan_id"
        );
    }

    // ─── ServicePlanIdentifier tests ───────────────────────────────────────────

    // Positive: default ctor generates a non‑null, valid UUID
    @Test
    void identifier_defaultConstructor_generatesValidUUID() {
        ServicePlanIdentifier id = new ServicePlanIdentifier();
        assertNotNull(id.getPlanId(), "Default ctor must set a non‑null UUID");
        // Should parse as UUID
        assertDoesNotThrow(() -> UUID.fromString(id.getPlanId()));
    }

    // Positive: all‑args ctor, setter & getter work
    @Test
    void identifier_allArgsConstructor_and_setter_getter_work() {
        ServicePlanIdentifier id = new ServicePlanIdentifier("MY‑PLAN");
        assertEquals("MY‑PLAN", id.getPlanId());

        id.setPlanId("NEW‑PLAN");
        assertEquals("NEW‑PLAN", id.getPlanId());
    }

    // Positive: equals, hashCode,  null / wrong‑type
    @Test
    void identifier_equals_hashCode_reflexive_null_wrongType() {
        ServicePlanIdentifier a = new ServicePlanIdentifier("X");
        ServicePlanIdentifier b = new ServicePlanIdentifier("X");
        ServicePlanIdentifier c = new ServicePlanIdentifier("Y");

        // equals / hashCode
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a.hashCode(), c.hashCode());

        // reflexive / null / wrong type
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "foo");
    }

    // ─── ServicePlan tests ────────────────────────────────────────────────────

    // Positive: all‑args ctor, no‑args ctor, getters and setters
    @Test
    void servicePlan_allArgsAndNoArgs_and_accessors() {
        // all‑args ctor
        ServicePlanIdentifier id = new ServicePlanIdentifier("P‑123");
        LocalDate exp = LocalDate.of(2030, 12, 31);

        ServicePlan p = new ServicePlan(42, id, "Cov", exp);
        assertEquals(42, p.getId());
        assertSame(id, p.getServicePlanIdentifier());
        assertEquals("Cov", p.getCoverageDetails());
        assertEquals(exp, p.getExpirationDate());

        // change via setters
        p.setId(99);
        p.setCoverageDetails("NEW");
        LocalDate later = exp.plusDays(1);
        p.setExpirationDate(later);

        assertEquals(99, p.getId());
        assertEquals("NEW", p.getCoverageDetails());
        assertEquals(later, p.getExpirationDate());

        // no‑args ctor
        ServicePlan empty = new ServicePlan();
        // default fields are null
        assertNull(empty.getServicePlanIdentifier());
        assertNull(empty.getCoverageDetails());
        assertNull(empty.getExpirationDate());
        // but we can set them
        empty.setServicePlanIdentifier(id);
        empty.setCoverageDetails("Else");
        empty.setExpirationDate(exp);
        assertSame(id, empty.getServicePlanIdentifier());
        assertEquals("Else", empty.getCoverageDetails());
        assertEquals(exp, empty.getExpirationDate());
    }

    // Positive: equals, hashCode, toString, reflexive / null / wrong‑type
    @Test
    void servicePlan_equals_hashCode_toString() {
        ServicePlanIdentifier id1 = new ServicePlanIdentifier("A");
        ServicePlanIdentifier id2 = new ServicePlanIdentifier("A");
        LocalDate d = LocalDate.of(2025, 1, 1);

        ServicePlan p1 = new ServicePlan(1, id1, "COV", d);
        ServicePlan p2 = new ServicePlan(1, id2, "COV", d);
        ServicePlan p3 = new ServicePlan(2, id1, "COV", d);

        // equals/hashCode
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1.hashCode(), p3.hashCode());

        // reflexive / null / wrong type
        assertEquals(p1, p1);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "foo");

        // toString contains key properties
        String ts = p1.toString();
        assertTrue(ts.contains("COV"));
        assertTrue(ts.contains("2025-01-01"));
    }


    // ─── Positive: application main should start without throwing under test profile ───────────────
    @Test
    void main_runsWithoutException() {
        assertDoesNotThrow(() ->
                ServicePlanServicesApplication.main(new String[]{})
        );
    }

    //unique exception

    // Positive: no‑arg ctor → null message
    @Test
    void noArgConstructor_hasNullMessage() {
        DuplicateCoverageDetailsException ex = new DuplicateCoverageDetailsException();
        assertNull(ex.getMessage());
    }

    // Positive: message ctor sets correct formatted message
    @Test
    void messageConstructor_setsCorrectMessage() {
        DuplicateCoverageDetailsException ex =
                new DuplicateCoverageDetailsException("Silver");
        assertEquals(
                "Coverage details Silver already exists.",
                ex.getMessage()
        );
    }

    // Positive: cause‑only ctor sets cause, leaves message to cause.toString()
    @Test
    void causeConstructor_setsCauseOnly() {
        Throwable cause = new RuntimeException("root");
        DuplicateCoverageDetailsException ex =
                new DuplicateCoverageDetailsException(cause);

        assertSame(cause, ex.getCause());
        // RuntimeException(Throwable) sets detailMessage = cause.toString()
        assertEquals(cause.toString(), ex.getMessage());
    }

    // Positive: message+cause ctor sets both fields
    @Test
    void messageAndCauseConstructor_setsBoth() {
        Throwable cause = new IllegalStateException();
        DuplicateCoverageDetailsException ex =
                new DuplicateCoverageDetailsException("Bronze", cause);
        assertEquals("Bronze", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @BeforeEach
    void seedDatabase() {
        repo.deleteAll();

        ServicePlan p1 = new ServicePlan();
        p1.setServicePlanIdentifier(new ServicePlanIdentifier(SP1));
        p1.setCoverageDetails("Full coverage for 1 year");
        p1.setExpirationDate(LocalDate.of(2026, 3, 16));
        repo.save(p1);

        ServicePlan p2 = new ServicePlan();
        p2.setServicePlanIdentifier(new ServicePlanIdentifier(SP2));
        p2.setCoverageDetails("Extended warranty (2 years)");
        p2.setExpirationDate(LocalDate.of(2027, 5, 10));
        repo.save(p2);
    }

}
