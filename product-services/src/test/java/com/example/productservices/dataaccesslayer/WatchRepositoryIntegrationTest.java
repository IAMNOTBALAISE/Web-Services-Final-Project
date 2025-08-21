package com.example.productservices.dataaccesslayer;

import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.watch.*;
import com.example.productservices.utils.exceptions.DuplicateCatalogTypeException;
import com.example.productservices.utils.exceptions.DuplicateWatchModelException;
import com.example.productservices.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class WatchRepositoryIntegrationTest {

    @Autowired
    private WatchRepository repo;

    private static final String VALID_ID    = "WCH-001";
    private static final String OTHER_ID    = "WCH-002";
    private static final String CATALOG_ID  = "catalog-001";
    private static final String OTHER_CATALOG  = "catalog-002";
    private static final String DUPLICATE_ID= "WCH-001";

    private Watch makeWatch(String watchId, String catalogId) {
        Watch w = new Watch();
        w.setWatchIdentifier(new WatchIdentifier(watchId));
        w.setCatalogIdentifier(new CatalogIdentifier(catalogId));
        w.setQuantity(10);
        w.setUsageType(UsageType.NEW);
        w.setModel("ModelX");
        w.setMaterial("Steel");
        w.setAccessories(List.of());
        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Brand");
        brand.setBrandCountry("Country");
        w.setWatchBrand(brand);
        w.setPrice(new Price(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO));
        return w;
    }

    // Positive: save then findByWatchIdentifier returns the correct watch
    @Test
    public void whenWatchExists_ReturnByWatchId() {
        Watch w = makeWatch(VALID_ID, CATALOG_ID);
        repo.save(w);

        Watch found = repo.findByWatchIdentifier_WatchId(VALID_ID);
        assertNotNull(found);
        assertEquals(VALID_ID, found.getWatchIdentifier().getWatchId());
    }

    // Positive: deleteAll then save two watches returns exactly two
    @Test
    public void whenWatchesExist_ReturnAllWatches() {
        repo.deleteAll();

        Watch w1 = makeWatch(VALID_ID, CATALOG_ID);
        Watch w2 = makeWatch(OTHER_ID, OTHER_CATALOG);
        repo.saveAll(List.of(w1, w2));

        List<Watch> all = repo.findAll();
        assertEquals(2, all.size());
    }

    // Positive: findAllByCatalogIdentifier returns only watches in that catalog
    @Test
    public void whenFindByCatalogId_ReturnFilteredList() {
        Watch w1 = makeWatch(VALID_ID, CATALOG_ID);
        Watch w2 = makeWatch(OTHER_ID, "other-cat");
        repo.saveAll(List.of(w1, w2));

        List<Watch> list = repo.findAllByCatalogIdentifier_CatalogId(CATALOG_ID);
        assertEquals(1, list.size());
        assertEquals(VALID_ID, list.get(0).getWatchIdentifier().getWatchId());
    }

    // Positive: existsByModelAndCatalogIdentifier returns true/false correctly
    @Test
    public void whenExistsByModelAndCatalogId_returnsTrueOtherwiseFalse() {
        Watch w = makeWatch(VALID_ID, CATALOG_ID);
        w.setModel("UniqueModel");
        repo.save(w);

        assertTrue(repo.existsByModelAndCatalogIdentifier_CatalogId("UniqueModel", CATALOG_ID));
        assertFalse(repo.existsByModelAndCatalogIdentifier_CatalogId("OtherModel", CATALOG_ID));
    }

    // Positive: updating a watch’s model is persisted
    @Test
    public void whenUpdateWatch_thenFieldIsPersisted() {
        Watch w = makeWatch(VALID_ID, CATALOG_ID);
        w.setModel("OldModel");
        repo.save(w);

        w.setModel("NewModel");
        repo.save(w);

        Watch reloaded = repo.findByWatchIdentifier_WatchId(VALID_ID);
        assertEquals("NewModel", reloaded.getModel());
    }

    // Positive: deleting one of two watches leaves exactly the other
    @Test
    public void whenDeleteWatch_thenGone() {
        Watch w1 = makeWatch(VALID_ID, CATALOG_ID);
        Watch w2 = makeWatch(OTHER_ID, OTHER_CATALOG);  // ← different catalog
        repo.saveAll(List.of(w1, w2));

        repo.delete(w1);
        List<Watch> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals(OTHER_ID, all.get(0).getWatchIdentifier().getWatchId());
    }

    // Negative: saving two watches with the same watchId violates unique constraint
    @Test
    public void whenSaveDuplicateIdentifier_thenThrowException() {
        Watch first = makeWatch(DUPLICATE_ID, CATALOG_ID);
        repo.saveAndFlush(first);

        Watch duplicate = makeWatch(DUPLICATE_ID, CATALOG_ID);
        assertThrows(DataIntegrityViolationException.class,
                () -> repo.saveAndFlush(duplicate));
    }

    // Negative: findByWatchIdentifier on non‑existing id returns null
    @Test
    public void whenFindByNonExistingId_returnsNull() {
        Watch found = repo.findByWatchIdentifier_WatchId("no-such");
        assertNull(found);
    }


    //utils

    // Positive Test: Ensures all constructors of DuplicateCatalogTypeException behave as expected
    @Test
    void testDuplicateCatalogTypeExceptionConstructors() {
        DuplicateCatalogTypeException e1 = new DuplicateCatalogTypeException();
        assertNotNull(e1);

        DuplicateCatalogTypeException e2 = new DuplicateCatalogTypeException("Smart Watch");
        assertEquals("Catalog with type Smart Watch already exists.", e2.getMessage());

        Throwable cause = new RuntimeException("Underlying cause");
        DuplicateCatalogTypeException e3 = new DuplicateCatalogTypeException(cause);
        assertEquals(cause, e3.getCause());

        DuplicateCatalogTypeException e4 = new DuplicateCatalogTypeException("Conflict", cause);
        assertEquals("Conflict", e4.getMessage());
        assertEquals(cause, e4.getCause());
    }

    // Positive Test: Ensures all constructors of DuplicateWatchModelException behave as expected
    @Test
    void testDuplicateWatchModelExceptionConstructors() {
        DuplicateWatchModelException e1 = new DuplicateWatchModelException();
        assertNotNull(e1);

        DuplicateWatchModelException e2 = new DuplicateWatchModelException("G-Shock");
        assertEquals("Watch with model G-Shock already exists.", e2.getMessage());

        Throwable cause = new IllegalArgumentException("Invalid model");
        DuplicateWatchModelException e3 = new DuplicateWatchModelException(cause);
        assertEquals(cause, e3.getCause());

        DuplicateWatchModelException e4 = new DuplicateWatchModelException("Error message", cause);
        assertEquals("Error message", e4.getMessage());
        assertEquals(cause, e4.getCause());
    }

    // Positive Test: Ensures all constructors of InvalidInputException behave as expected
    @Test
    void testInvalidInputExceptionConstructors() {
        InvalidInputException e1 = new InvalidInputException();
        assertNotNull(e1);

        InvalidInputException e2 = new InvalidInputException("Bad input");
        assertEquals("Bad input", e2.getMessage());

        Throwable cause = new NullPointerException("Null input");
        InvalidInputException e3 = new InvalidInputException(cause);
        assertEquals(cause, e3.getCause());

        InvalidInputException e4 = new InvalidInputException("Invalid field", cause);
        assertEquals("Invalid field", e4.getMessage());
        assertEquals(cause, e4.getCause());
    }
}
