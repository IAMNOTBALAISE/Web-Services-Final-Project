package com.example.productservices.presentationlayer;

import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.dataccesslayer.watch.*;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchRequestModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchResponseModel;
import com.example.productservices.utils.HttpErrorInfo;
import com.example.productservices.utils.exceptions.DuplicateWatchModelException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/schema-h2.sql", "/data-h2.sql"})
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WatchControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String BASE_URL = "/api/v1/watches";

    // Positive Test: Fetch existing watch by ID
    @Test
    public void getWatchById_existingWatch_returnsWatch() {

        String watchId = "WCH-001";
        ResponseEntity<WatchResponseModel> response = restTemplate.getForEntity(BASE_URL + "/" + watchId, WatchResponseModel.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(watchId, response.getBody().getWatchId());
    }

    // Negative Test: Fetch non-existent watch ID
    @Test
    public void getWatchById_nonExistingWatch_returnsUnprocessableEntity() {
        String watchId = "non-existent-id";
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + watchId, String.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(
                response.getBody().toLowerCase().contains("not found") ||
                        response.getBody().toLowerCase().contains("unknown") ||
                        response.getBody().toLowerCase().contains("does not exist")
        );
    }

    // Positive Test: Retrieve all watches with no filters
    @Test
    public void getWatchesWithNoFilter_returnsAllWatches() {
        ResponseEntity<WatchResponseModel[]> response = restTemplate.getForEntity(BASE_URL, WatchResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    // Positive Test: Query watches using a filter (but filters ignored)
    @Test
    public void getWatchesWithFilter_returnsAllWatchesIgnoringFilter() {
        String filterValue = "USA";  // Pretend to filter
        String url = BASE_URL + "?brandCountry=" + filterValue;

        ResponseEntity<WatchResponseModel[]> response = restTemplate.getForEntity(url, WatchResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1, "Expected at least one watch in the result");

        System.out.println("Watches returned:");
        for (WatchResponseModel watch : response.getBody()) {
            System.out.println(" - " + watch.getWatchBrand().getBrandName() +
                    " (" + watch.getWatchBrand().getBrandCountry() + ")");
        }
    }

    //  Positive Test: Get watches by valid filter (brand name)
    @Test
    public void getWatchesWithFilter_validBrandName_returnsAtLeastOneMatchingWatch() {
        String brandName = "Apple";
        String url = BASE_URL + "?brandName=" + brandName;

        ResponseEntity<WatchResponseModel[]> response = restTemplate.getForEntity(url, WatchResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        boolean foundMatch = false;
        for (WatchResponseModel watch : response.getBody()) {
            if (brandName.equals(watch.getWatchBrand().getBrandName())) {
                foundMatch = true;
                break;
            }
        }

        assertTrue(foundMatch, "At least one watch should match the brand name: " + brandName);
    }

    // Positive Test: Get watch by valid ID and check nested accessory list
    @Test
    public void getWatchById_checkAccessories_notEmpty() {
        String watchId = "WCH-001";
        ResponseEntity<WatchResponseModel> response = restTemplate.getForEntity(BASE_URL + "/" + watchId, WatchResponseModel.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessories());
        assertTrue(response.getBody().getAccessories().size() > 0);
    }

    //  Negative Test: Use unsupported query parameter and ensure response still OK (no filtering)
    @Test
    public void getWatchesWithFilter_unsupportedParameter_ignoredGracefully() {
        String url = BASE_URL + "?unsupportedField=value";

        ResponseEntity<WatchResponseModel[]> response = restTemplate.getForEntity(url, WatchResponseModel[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0); // should return all watches
    }

    //  Negative Test: Invalid ID format (e.g., special characters)
    @Test
    public void getWatchById_invalidFormat_returnsUnprocessableEntity() {
        String watchId = "%%$$@@"; // clearly invalid format
        ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/" + watchId, String.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toLowerCase().contains("invalid") ||
                response.getBody().toLowerCase().contains("does not exist"));
    }

    // Positive Test: Build and validate Watch object manually (setters)
    @Test
    public void testWatchObjectCreationAndEquality_withSettersOnly() {
        // Create embedded objects and set fields
        WatchIdentifier id = new WatchIdentifier();
        id.setWatchId("WCH-999");

        CatalogIdentifier catalogId = new CatalogIdentifier();
        catalogId.setCatalogId("catalog-003");

        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Casio");
        brand.setBrandCountry("Japan");

        Price price = new Price();
        price.setMsrp(new BigDecimal("100"));
        price.setCost(new BigDecimal("80"));
        price.setTotalOptionsCost(new BigDecimal("20"));

        Accessory accessory = new Accessory();
        accessory.setAccessoryName("Rubber Strap");
        accessory.setAccessoryCost(new BigDecimal("10"));

        List<Accessory> accessories = Collections.singletonList(accessory);

        // Create and set Watch
        Watch watch = new Watch();
        watch.setWatchIdentifier(id);
        watch.setCatalogIdentifier(catalogId);
        watch.setQuantity(1);
        watch.setUsageType(UsageType.NEW);
        watch.setModel("G-Shock");
        watch.setMaterial("Resin");
        watch.setAccessories(accessories);
        watch.setWatchBrand(brand);
        watch.setPrice(price);

        // Validate
        assertEquals("G-Shock", watch.getModel());
        assertEquals("Casio", watch.getWatchBrand().getBrandName());
        assertEquals("Rubber Strap", watch.getAccessories().get(0).getAccessoryName());
        assertEquals(new BigDecimal("100"), watch.getPrice().getMsrp());
    }

    // Positive Test: Simple POJO setters, equals, toString
    @Test
    public void testPojoMethods_withSettersAndToStringEquals() {
        Accessory accessory = new Accessory();
        accessory.setAccessoryName("Steel Band");
        accessory.setAccessoryCost(new BigDecimal("50"));
        assertEquals("Steel Band", accessory.getAccessoryName());

        Price price = new Price();
        price.setMsrp(new BigDecimal("200"));
        price.setCost(new BigDecimal("150"));
        price.setTotalOptionsCost(new BigDecimal("50"));
        assertEquals(new BigDecimal("150"), price.getCost());

        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Seiko");
        brand.setBrandCountry("Japan");
        assertEquals("Japan", brand.getBrandCountry());

        WatchIdentifier watchId = new WatchIdentifier();
        watchId.setWatchId("WCH-123");
        assertEquals("WCH-123", watchId.getWatchId());

        // Validate toString + equals
        assertNotNull(price.toString());
        assertNotNull(brand.toString());
        assertNotNull(accessory.toString());

        WatchBrand expectedBrand = new WatchBrand();
        expectedBrand.setBrandName("Seiko");
        expectedBrand.setBrandCountry("Japan");
        assertTrue(brand.equals(expectedBrand));
    }

    // Positive Test: POJO full constructor and equals logic
    @Test
    public void testPojoCoverage() {
        WatchIdentifier watchId = new WatchIdentifier("WCH-101");
        assertEquals("WCH-101", watchId.getWatchId());
        assertNotNull(watchId.toString());
        assertEquals(watchId, new WatchIdentifier("WCH-101"));

        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Seiko");
        brand.setBrandCountry("Japan");
        assertEquals("Seiko", brand.getBrandName());
        assertEquals("Japan", brand.getBrandCountry());
        assertNotNull(brand.toString());

        WatchBrand comparisonBrand = new WatchBrand();
        comparisonBrand.setBrandName("Seiko");
        comparisonBrand.setBrandCountry("Japan");
        assertTrue(brand.equals(comparisonBrand));

        Accessory acc = new Accessory("Leather Strap", new BigDecimal("100"));
        assertEquals("Leather Strap", acc.getAccessoryName());
        assertEquals(new BigDecimal("100"), acc.getAccessoryCost());
        assertNotNull(acc.toString());
        assertTrue(acc.equals(new Accessory("Leather Strap", new BigDecimal("100"))));

        Price price = new Price(new BigDecimal("1500"), new BigDecimal("1000"), new BigDecimal("500"));
        assertEquals(new BigDecimal("1500"), price.getMsrp());
        assertEquals(new BigDecimal("1000"), price.getCost());
        assertEquals(new BigDecimal("500"), price.getTotalOptionsCost());
        assertNotNull(price.toString());
        assertTrue(price.equals(new Price(new BigDecimal("1500"), new BigDecimal("1000"), new BigDecimal("500"))));

        // Build Watch using setters (not constructor)
        Watch watch = new Watch();
        watch.setWatchIdentifier(watchId);
        watch.setCatalogIdentifier(new CatalogIdentifier("catalog-999"));
        watch.setQuantity(1);
        watch.setUsageType(UsageType.NEW);
        watch.setModel("Rolex Submariner");
        watch.setMaterial("Steel");
        watch.setAccessories(List.of(acc));
        watch.setWatchBrand(brand);
        watch.setPrice(price);

        assertEquals("Rolex Submariner", watch.getModel());
        assertEquals("Steel", watch.getMaterial());
        assertEquals(1, watch.getQuantity());
        assertEquals(UsageType.NEW, watch.getUsageType());
        assertEquals(price, watch.getPrice());
        assertEquals(brand, watch.getWatchBrand());
        assertEquals(watchId, watch.getWatchIdentifier());
        assertEquals("catalog-999", watch.getCatalogIdentifier().getCatalogId());
        assertNotNull(watch.toString());
    }

    // Positive Test: Validate Price object behavior
    @Test
    public void testPriceEqualityAndToString() {
        Price p1 = new Price(new BigDecimal("999"), new BigDecimal("888"), new BigDecimal("111"));
        Price p2 = new Price(new BigDecimal("999"), new BigDecimal("888"), new BigDecimal("111"));

        assertEquals(p1, p2); // equals()
        assertEquals(p1.hashCode(), p2.hashCode()); // hashCode()
        assertNotNull(p1.toString()); // toString()
    }

    // Positive Test: Validate Accessory equals and hashCode
    @Test
    public void testAccessoryEqualityAndHashCode() {
        Accessory a1 = new Accessory("Rubber", new BigDecimal("75"));
        Accessory a2 = new Accessory("Rubber", new BigDecimal("75"));
        Accessory a3 = new Accessory("Leather", new BigDecimal("100"));

        assertEquals(a1, a2);         // equal objects
        assertNotEquals(a1, a3);      // not equal
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotNull(a1.toString());
    }
    // Positive Test: Validate Accessory equals and hashCode
    @Test
    public void testWatchBrandEdgeCases() {
        WatchBrand brand1 = new WatchBrand();
        brand1.setBrandName("Fossil");
        brand1.setBrandCountry("USA");

        WatchBrand brand2 = new WatchBrand();
        brand2.setBrandName("Fossil");
        brand2.setBrandCountry("USA");

        WatchBrand brand3 = new WatchBrand("Omega", "Switzerland");

        assertEquals(brand1, brand2);     // same data
        assertNotEquals(brand1, brand3);  // different data
        assertNotNull(brand1.toString());
    }

    // Positive Test: Construct watch using valid subcomponents
    @Test
    public void testWatchManualSetterCoverage_withRealDataReference() {
        // Create subcomponents
        Watch watch = new Watch();

        WatchIdentifier watchId = new WatchIdentifier("WCH-001");
        watch.setWatchIdentifier(watchId);

        CatalogIdentifier catalogId = new CatalogIdentifier("catalog-001");
        watch.setCatalogIdentifier(catalogId);

        watch.setQuantity(1); // Matches SQL
        watch.setUsageType(UsageType.NEW);           // Matches SQL
        watch.setModel("Apple Watch Ultra");
        watch.setMaterial("Aluminum");

        Accessory acc1 = new Accessory("Sapphire Crystal", new BigDecimal("120.00"));
        Accessory acc2 = new Accessory("Titanium Sport Band", new BigDecimal("80.00"));
        watch.setAccessories(List.of(acc1, acc2));

        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Apple");
        brand.setBrandCountry("USA");
        watch.setWatchBrand(brand);

        Price price = new Price(
                new BigDecimal("1200.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("200.00")
        );
        watch.setPrice(price);

        // Validate fields
        assertEquals("WCH-001", watch.getWatchIdentifier().getWatchId());
        assertEquals("catalog-001", watch.getCatalogIdentifier().getCatalogId());
        assertEquals("Apple Watch Ultra", watch.getModel());
        assertEquals("Aluminum", watch.getMaterial());
        assertEquals(1, watch.getQuantity());
        assertEquals(UsageType.NEW, watch.getUsageType());

        assertEquals(2, watch.getAccessories().size());
        assertEquals("Sapphire Crystal", watch.getAccessories().get(0).getAccessoryName());
        assertEquals("Titanium Sport Band", watch.getAccessories().get(1).getAccessoryName());

        assertEquals("Apple", watch.getWatchBrand().getBrandName());
        assertEquals("USA", watch.getWatchBrand().getBrandCountry());

        assertEquals(new BigDecimal("1200.00"), watch.getPrice().getMsrp());
        assertEquals(new BigDecimal("1000.00"), watch.getPrice().getCost());
        assertEquals(new BigDecimal("200.00"), watch.getPrice().getTotalOptionsCost());

        assertNotNull(watch.toString());
    }


    // Positive Test: All-args constructor usage
    @Test
    public void testWatchAllArgsConstructor() {
        WatchIdentifier watchIdentifier = new WatchIdentifier("WCH-007");
        CatalogIdentifier catalogIdentifier = new CatalogIdentifier("catalog-003");

        Integer quantity = 1;
        UsageType usage = UsageType.NEW;
        String model = "Casio G-Shock";
        String material = "Resin";

        Accessory accessory = new Accessory("Protection Cover", new BigDecimal("20.00"));
        List<Accessory> accessories = List.of(accessory);

        WatchBrand brand = new WatchBrand("Casio", "Japan");
        Price price = new Price(new BigDecimal("150.00"), new BigDecimal("100.00"), new BigDecimal("50.00"));

        Watch watch = new Watch(
                123, // some ID
                watchIdentifier,
                catalogIdentifier,
                quantity,
                usage,
                model,
                material,
                accessories,
                brand,
                price
        );

        assertNotNull(watch);
        assertEquals("Casio G-Shock", watch.getModel());
        assertEquals("Casio", watch.getWatchBrand().getBrandName());
    }

    // Positive Test: Validate equals/hashCode/toString for Watch
    @Test
    public void testWatchDataMethodsCoverage() {
        Watch w1 = new Watch();
        w1.setModel("G-Shock");
        w1.setMaterial("Resin");

        Watch w2 = new Watch();
        w2.setModel("G-Shock");
        w2.setMaterial("Resin");

        // Test equality when all fields match
        assertEquals(w1, w2);

        // Now change one field to test inequality
        w2.setMaterial("Titanium");
        assertNotEquals(w1, w2);

        // toString and hashCode coverage
        assertNotNull(w1.toString());
        assertNotNull(w1.hashCode());
    }

    // Positive Test: Use Lombok-style data features and validate
    @Test
    public void testLombokDataFeaturesForWatch() {
        WatchIdentifier id = new WatchIdentifier("WCH-101");
        CatalogIdentifier catalogId = new CatalogIdentifier("catalog-123");
        WatchBrand brand = new WatchBrand("Casio", "Japan");
        Accessory acc = new Accessory("Leather Strap", new BigDecimal("150"));
        Price price = new Price(new BigDecimal("1200"), new BigDecimal("1000"), new BigDecimal("200"));

        Watch watch1 = new Watch();
        watch1.setWatchIdentifier(id);
        watch1.setCatalogIdentifier(catalogId);
        watch1.setQuantity(1);
        watch1.setUsageType(UsageType.NEW);
        watch1.setModel("G-Shock");
        watch1.setMaterial("Resin");
        watch1.setAccessories(List.of(acc));
        watch1.setWatchBrand(brand);
        watch1.setPrice(price);

        // Use getters to validate
        assertEquals("WCH-101", watch1.getWatchIdentifier().getWatchId());
        assertEquals("catalog-123", watch1.getCatalogIdentifier().getCatalogId());
        assertEquals("G-Shock", watch1.getModel());
        assertEquals("Resin", watch1.getMaterial());
        assertEquals(1, watch1.getQuantity());
        assertEquals(UsageType.NEW, watch1.getUsageType());
        assertEquals("Casio", watch1.getWatchBrand().getBrandName());
        assertEquals("Japan", watch1.getWatchBrand().getBrandCountry());
        assertEquals(new BigDecimal("1200"), watch1.getPrice().getMsrp());
        assertEquals("Leather Strap", watch1.getAccessories().get(0).getAccessoryName());

        // Test toString
        assertNotNull(watch1.toString());
        assertTrue(watch1.toString().contains("G-Shock"));

        // Test equals and hashCode
        Watch watch2 = new Watch();
        watch2.setWatchIdentifier(id);
        watch2.setCatalogIdentifier(catalogId);
        watch2.setQuantity(1);
        watch2.setUsageType(UsageType.NEW);
        watch2.setModel("G-Shock");
        watch2.setMaterial("Resin");
        watch2.setAccessories(List.of(acc));
        watch2.setWatchBrand(brand);
        watch2.setPrice(price);

        assertEquals(watch1, watch2);  // All fields are same
        assertEquals(watch1.hashCode(), watch2.hashCode());
    }

    // Positive Test: Edge cases for equals/hashCode
    @Test
    public void testEqualsAndHashCodeEdgeCases() {
        WatchIdentifier id1 = new WatchIdentifier("WCH-101");
        CatalogIdentifier cat1 = new CatalogIdentifier("catalog-123");
        WatchBrand brand1 = new WatchBrand("Casio", "Japan");
        Accessory acc = new Accessory("Strap", new BigDecimal("50"));
        Price price1 = new Price(new BigDecimal("1000"), new BigDecimal("900"), new BigDecimal("100"));

        Watch watch1 = new Watch();
        watch1.setWatchIdentifier(id1);
        watch1.setCatalogIdentifier(cat1);
        watch1.setQuantity(1);
        watch1.setUsageType(UsageType.NEW);
        watch1.setModel("G-Shock");
        watch1.setMaterial("Resin");
        watch1.setAccessories(List.of(acc));
        watch1.setWatchBrand(brand1);
        watch1.setPrice(price1);

        // Self comparison
        assertEquals(watch1, watch1);

        // Comparison to null and different type
        assertNotEquals(watch1, null);
        assertNotEquals(watch1, new Object());

        // Different object (at least one field is different)
        Watch watch2 = new Watch();
        watch2.setWatchIdentifier(new WatchIdentifier("WCH-999"));  // different ID
        watch2.setCatalogIdentifier(cat1);
        watch2.setQuantity(1);
        watch2.setUsageType(UsageType.NEW);
        watch2.setModel("G-Shock");
        watch2.setMaterial("Resin");
        watch2.setAccessories(List.of(acc));
        watch2.setWatchBrand(brand1);
        watch2.setPrice(price1);

        assertNotEquals(watch1, watch2);

        // Symmetry
        Watch watch3 = new Watch();
        watch3.setWatchIdentifier(id1);
        watch3.setCatalogIdentifier(cat1);
        watch3.setQuantity(1);
        watch3.setUsageType(UsageType.NEW);
        watch3.setModel("G-Shock");
        watch3.setMaterial("Resin");
        watch3.setAccessories(List.of(acc));
        watch3.setWatchBrand(brand1);
        watch3.setPrice(price1);

        assertEquals(watch1, watch3);
        assertEquals(watch3, watch1);
        assertEquals(watch1.hashCode(), watch3.hashCode());
    }

    // Positive Test: Build WatchRequestModel using builder
    @Test
    void testWatchRequestModelBuilder() {
        // Create nested components
        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Fossil");
        brand.setBrandCountry("USA");

        Price price = new Price(
                new BigDecimal("250"),
                new BigDecimal("180"),
                new BigDecimal("70")
        );

        Accessory accessory = new Accessory("Extra Band", new BigDecimal("30"));

        // Use the builder to create the WatchRequestModel
        WatchRequestModel model = WatchRequestModel.builder()
                .catalogId("catalog-123")
                .model("Fossil Q")
                .material("Leather")
                .quantity(1)
                .usageType(UsageType.NEW)
                .accessories(List.of(accessory))
                .price(price)
                .watchBrand(brand)
                .build();

        // Assertions
        assertEquals("Fossil Q", model.getModel());
        assertEquals("Leather", model.getMaterial());
        assertEquals("catalog-123", model.getCatalogId());
        assertEquals(1, model.getQuantity());
        assertEquals(UsageType.NEW, model.getUsageType());

        assertNotNull(model.getWatchBrand());
        assertEquals("Fossil", model.getWatchBrand().getBrandName());
        assertEquals("USA", model.getWatchBrand().getBrandCountry());

        assertNotNull(model.getPrice());
        assertEquals(new BigDecimal("250"), model.getPrice().getMsrp());

        assertNotNull(model.getAccessories());
        assertEquals("Extra Band", model.getAccessories().get(0).getAccessoryName());

        // Optional: toString coverage
        assertNotNull(model.toString());
    }

    // Negative Test: Creating a watch with a model that already exists should return 422
    @Test
    public void createDuplicateWatch_returnsUnprocessableEntity() {
        // 1) Fetch an existing watch so we know the model & catalog
        ResponseEntity<WatchResponseModel> existingResp =
                restTemplate.getForEntity(BASE_URL + "/WCH-001", WatchResponseModel.class);
        WatchResponseModel existing = existingResp.getBody();
        assertNotNull(existing);

        // 2) Build a duplicate request
        WatchRequestModel dupReq = WatchRequestModel.builder()
                .catalogId(existing.getCatalogId())     // ← used in the path too
                .model(existing.getModel())
                .material(existing.getMaterial())
                .quantity(existing.getQuantity())
                .usageType(existing.getUsageType())
                .accessories(existing.getAccessories())
                .watchBrand(existing.getWatchBrand())
                .price(existing.getPrice())
                .build();

        // 3) POST against the **catalog**-scoped URL, not `/api/v1/watches`
        String addUrl = "/api/v1/catalogs/{catalogId}/watches";
        ResponseEntity<String> response = restTemplate.postForEntity(
                addUrl,
                dupReq,
                String.class,
                Collections.singletonMap("catalogId", existing.getCatalogId())
        );

        // 4) We now get the expected 422
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertTrue(response.getBody().toLowerCase().contains("already exists"),
                "Expected duplicate-model error");
    }






}