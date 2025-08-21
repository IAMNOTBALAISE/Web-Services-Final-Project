package com.example.productservices.dataaccesslayer;

import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.catalog.CatalogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CatalogRepositoryIntegrationTest {

    @Autowired
    private CatalogRepository repo;

    private static final String VALID_ID  = "catalog-001";
    private static final String OTHER_ID  = "catalog-002";
    private static final String DUPLICATE_ID = "catalog-001";

    // Positive: saving then retrieving by catalogId returns the correct catalog
    @Test
    public void whenCatalogExists_ReturnByCatalogId() {

        Catalog c = new Catalog();
        c.setCatalogIdentifier(new CatalogIdentifier(VALID_ID));
        c.setType("TypeA");
        c.setDescription("DescA");
        repo.save(c);

        Catalog found = repo.findByCatalogIdentifier_CatalogId(VALID_ID);
        assertNotNull(found);
        assertEquals(VALID_ID, found.getCatalogIdentifier().getCatalogId());
    }

    // Positive: deleting all then saving two returns exactly two
    @Test
    public void whenCatalogsExist_ReturnAllCatalogs() {

        repo.deleteAll();

        Catalog c1 = new Catalog();
        c1.setCatalogIdentifier(new CatalogIdentifier(VALID_ID));
        Catalog c2 = new Catalog();
        c2.setCatalogIdentifier(new CatalogIdentifier(OTHER_ID));
        repo.saveAll(List.of(c1, c2));

        List<Catalog> all = repo.findAll();
        assertEquals(2, all.size());
    }

    // Positive: existsByCatalogIdentifier returns true for saved ID, false otherwise
    @Test
    public void whenExistsByIdentifier_returnsTrueOtherwiseFalse() {

        Catalog c = new Catalog();
        c.setCatalogIdentifier(new CatalogIdentifier(VALID_ID));
        repo.save(c);
        assertTrue(repo.existsByCatalogIdentifier_CatalogId(VALID_ID));
        assertFalse(repo.existsByCatalogIdentifier_CatalogId("no-such"));
    }

    // Positive: existsByType returns true for saved type, false otherwise
    @Test
    public void whenExistsByType_returnsTrueOtherwiseFalse() {
        Catalog c = new Catalog();
        c.setCatalogIdentifier(new CatalogIdentifier(VALID_ID));
        c.setType("UniqueType");
        repo.save(c);
        assertTrue(repo.existsByType("UniqueType"));
        assertFalse(repo.existsByType("OtherType"));
    }

    // Positive: updating a catalog’s type is persisted
    @Test
    public void whenUpdateCatalog_thenTypeIsPersisted() {
        Catalog c = new Catalog();
        c.setCatalogIdentifier(new CatalogIdentifier(VALID_ID));
        c.setType("OldType");
        repo.save(c);

        c.setType("NewType");
        repo.save(c);

        Catalog reloaded = repo.findByCatalogIdentifier_CatalogId(VALID_ID);
        assertEquals("NewType", reloaded.getType());
    }

    // Positive: deleting one of two catalogs leaves the other
    @Test
    public void whenDeleteCatalog_thenGone() {
        Catalog a = new Catalog();
        a.setCatalogIdentifier(new CatalogIdentifier(VALID_ID));
        Catalog b = new Catalog();
        b.setCatalogIdentifier(new CatalogIdentifier(OTHER_ID));
        repo.saveAll(List.of(a, b));

        repo.delete(a);
        List<Catalog> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals(OTHER_ID, all.get(0).getCatalogIdentifier().getCatalogId());
    }

    // Negative: saving two catalogs with the same catalogId violates unique constraint
    @Test
    public void whenSaveDuplicateIdentifier_thenThrowException() {
        Catalog first = new Catalog();
        first.setCatalogIdentifier(new CatalogIdentifier(DUPLICATE_ID));
        repo.saveAndFlush(first);

        Catalog duplicate = new Catalog();
        duplicate.setCatalogIdentifier(new CatalogIdentifier(DUPLICATE_ID));
        assertThrows(DataIntegrityViolationException.class,
                () -> repo.saveAndFlush(duplicate));
    }

    // Negative: findByCatalogIdentifier on non‑existing id returns null
    @Test
    public void whenFindByNonExistingId_returnsNull() {
        Catalog found = repo.findByCatalogIdentifier_CatalogId("no-such");
        assertNull(found);
    }
}
