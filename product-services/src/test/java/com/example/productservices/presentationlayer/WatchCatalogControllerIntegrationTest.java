package com.example.productservices.presentationlayer;

import com.example.productservices.ProductServicesApplication;
import com.example.productservices.dataccesslayer.watch.*;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogRequestModel;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogResponseModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchRequestModel;
import com.example.productservices.presentationlayer.WatchPresentationLayer.WatchResponseModel;
import com.example.productservices.utils.GlobalControllerExceptionHandler;
import com.example.productservices.utils.HttpErrorInfo;
import com.example.productservices.utils.exceptions.InvalidInputException;
import com.example.productservices.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.context.request.WebRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/schema-h2.sql", "/data-h2.sql"})
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WatchCatalogControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private GlobalControllerExceptionHandler exceptionHandler;

    @MockBean
    private WebRequest mockWebRequest;

    private static final String BASE_CATALOG     = "/api/v1/catalogs";
    private static final String VALID_CATALOG_ID = "catalog-001";

    @BeforeEach
    void setupMockWebRequest() {
        when(mockWebRequest.getDescription(false))
                .thenReturn("uri=/dummy/path");
    }

    // ─── Positive: get all watches on a new catalog returns empty ───────────
    @Test
    public void getAllWatches_initiallyEmpty() {
        // 1. Fetch whatever seed data is present
        List<WatchResponseModel> existing = webClient.get()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(existing);

        // 2. Delete each seeded watch
        for (WatchResponseModel w : existing) {
            webClient.delete()
                    .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches/" + w.getWatchId())
                    .exchange()
                    // expect 204 now instead of 200
                    .expectStatus().isNoContent();
        }

        // 3. Now it really is empty
        webClient.get()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .value(list -> assertTrue(list.isEmpty()));
    }
    // ─── Positive: initial seeded H2 has one watch in catalog-001 ───────────
    @Test
    public void getAllWatches_initialSeeded_returnsOne() {
        webClient.get()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    WatchResponseModel w = list.get(0);
                    assertEquals("WCH-001",      w.getWatchId());
                    assertEquals("catalog-001",  w.getCatalogId());
                    assertEquals(UsageType.NEW,  w.getUsageType());
                    assertEquals("Apple Watch Ultra", w.getModel());
                    assertEquals("Aluminum",     w.getMaterial());

                    // Numeric comparison ignoring scale
                    assertEquals(0,
                            w.getPrice().getMsrp()
                                    .compareTo(BigDecimal.valueOf(1200.00))
                    );

                    assertEquals("Apple",        w.getWatchBrand().getBrandName());
                    assertEquals("USA",          w.getWatchBrand().getBrandCountry());
                    assertEquals(1, w.getQuantity()); // changed line
                });
    }

    // ─── Positive: add a new watch, returns 201 Created and correct body ─────
    @Test
    public void addWatch_validRequest_returnsCreated() {
        WatchRequestModel req = new WatchRequestModel();

        WatchBrand brand = new WatchBrand();
        brand.setBrandName("Omega");
        brand.setBrandCountry("Switzerland");
        req.setWatchBrand(brand);

        Price price = new Price();
        price.setMsrp(BigDecimal.valueOf(1200.00));
        price.setCost(BigDecimal.valueOf(1000.00));
        price.setTotalOptionsCost(BigDecimal.ZERO);
        req.setPrice(price);

        req.setUsageType(UsageType.NEW);
        req.setModel("ModelX");
        req.setMaterial("Aluminum");
        req.setQuantity(1); // changed line

        webClient.post()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError();
    }


   // ─── Positive: after adding, get all returns one entry ───────────────────
   @Test
   public void getAllWatches_afterAdd_returnsOne() {
       // 1) start from the seeded catalog (catalog-001) and remove its one seed watch
       webClient.delete()
               .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches/WCH-001")
               .exchange()
               // <-- expect 204 now instead of 200
               .expectStatus().isNoContent();

       // 2) add the new watch
       WatchRequestModel req = new WatchRequestModel();
       WatchBrand b = new WatchBrand();
       b.setBrandName("TagHeuer");
       b.setBrandCountry("Switzerland");
       req.setWatchBrand(b);

       Price p1 = new Price();
       p1.setMsrp(BigDecimal.valueOf(600.00));
       p1.setCost(BigDecimal.valueOf(500.00));
       p1.setTotalOptionsCost(BigDecimal.ZERO);
       req.setPrice(p1);

       req.setUsageType(UsageType.NEW);
       req.setQuantity(1);
       req.setModel("Carrera");
       req.setMaterial("Steel");

       webClient.post()
               .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
               .contentType(MediaType.APPLICATION_JSON)
               .bodyValue(req)
               .exchange()
               .expectStatus().isCreated();

       // 3) now GET and assert exactly one
       webClient.get()
               .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
               .exchange()
               .expectStatus().isOk()
               .expectBodyList(WatchResponseModel.class)
               .value(list -> {
                   assertEquals(1, list.size());
                   WatchResponseModel w = list.get(0);
                   assertEquals("TagHeuer",    w.getWatchBrand().getBrandName());
                   assertEquals("Switzerland", w.getWatchBrand().getBrandCountry());
                   assertEquals("Carrera",     w.getModel());
                   assertEquals("Steel",       w.getMaterial());
               });
   }
    // ─── Positive: GET with query‐param filtering ────────────────────────────
    @Test
    public void getWatches_withFilter_returnsFiltered() {
        String catalogId = VALID_CATALOG_ID;

        // Step 0: delete all existing watches in the catalog
        List<WatchResponseModel> existing = webClient.get()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();

        for (WatchResponseModel w : existing) {
            webClient.delete()
                    .uri(BASE_CATALOG + "/" + catalogId + "/watches/" + w.getWatchId())
                    .exchange()
                    // <-- expect 204 now instead of 200
                    .expectStatus().isNoContent();
        }

        // Step 1: add new A/X watch
        WatchRequestModel r1 = new WatchRequestModel();
        WatchBrand brand1 = new WatchBrand();
        brand1.setBrandName("A");
        brand1.setBrandCountry("X");
        r1.setWatchBrand(brand1);

        Price p1 = new Price();
        p1.setMsrp(BigDecimal.valueOf(100.00));
        p1.setCost(BigDecimal.valueOf(80.00));
        p1.setTotalOptionsCost(BigDecimal.ZERO);
        r1.setPrice(p1);

        r1.setUsageType(UsageType.USED);
        r1.setQuantity(1);
        r1.setModel("M1");
        r1.setMaterial("Mat1");

        webClient.post()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(r1)
                .exchange()
                .expectStatus().isCreated();

        // Step 2: filter by brand name
        webClient.get()
                .uri(uri -> uri
                        .path(BASE_CATALOG + "/" + catalogId + "/watches")
                        .queryParam("watchBrand.brandName", "A")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size(), "should return exactly the one watch we just added");
                    assertEquals("A", list.get(0).getWatchBrand().getBrandName());
                    assertEquals("X", list.get(0).getWatchBrand().getBrandCountry());
                });
    }
    // ─── Positive: get by ID after add returns that watch ────────────────────
    @Test
    public void getWatchById_afterAdd_returnsWatch() {
        String catalogId = VALID_CATALOG_ID;

        // Step 0: delete all existing watches in the catalog
        List<WatchResponseModel> existing = webClient.get()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();

        for (WatchResponseModel w : existing) {
            webClient.delete()
                    .uri(BASE_CATALOG + "/" + catalogId + "/watches/" + w.getWatchId())
                    .exchange()
                    // ← expect 204 instead of 200
                    .expectStatus().isNoContent();
        }

        // Step 1: create Rolex watch
        WatchRequestModel req = new WatchRequestModel();
        WatchBrand br = new WatchBrand();
        br.setBrandName("Rolex");
        br.setBrandCountry("Switzerland");
        req.setWatchBrand(br);

        Price pr = new Price();
        pr.setMsrp(BigDecimal.valueOf(5000.00));
        pr.setCost(BigDecimal.valueOf(4500.00));
        pr.setTotalOptionsCost(BigDecimal.ZERO);
        req.setPrice(pr);

        req.setUsageType(UsageType.NEW);
        req.setQuantity(5);
        req.setModel("Submariner");
        req.setMaterial("Gold");

        // Step 2: post the watch
        WatchResponseModel added = webClient.post()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(added);
        assertNotNull(added.getWatchId());

        // Step 3: fetch by ID and verify
        webClient.get()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches/" + added.getWatchId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(WatchResponseModel.class)
                .value(resp -> assertEquals(
                        added.getWatchId(), resp.getWatchId()
                ));
    }

    // ─── Negative: get by ID for non‐existent watch returns 404 ─────────────
    @Test
    public void getWatchById_notFound_returnsUnprocessableEntity() {
        String catalogId = VALID_CATALOG_ID;

        // Clean the catalog
        List<WatchResponseModel> existing = webClient.get()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();

        for (WatchResponseModel w : existing) {
            webClient.delete()
                    .uri(BASE_CATALOG + "/" + catalogId + "/watches/" + w.getWatchId())
                    .exchange()
                    // expect 204 instead of 200
                    .expectStatus().isNoContent();
        }

        // Now test non-existent watch
        webClient.get()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches/nonexistent")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("does not exist"));
                    assertEquals("uri=" + BASE_CATALOG + "/" + catalogId + "/watches/nonexistent", info.getPath());
                });
    }
    // ─── Positive: update an existing watch returns 200 OK and updated body ──
    @Test
    public void updateWatch_existing_returnsUpdated() {
        String catalogId = VALID_CATALOG_ID;

        // Step 1: Clean catalog to ensure isolated test
        List<WatchResponseModel> existing = webClient.get()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();

        for (WatchResponseModel w : existing) {
            webClient.delete()
                    .uri(BASE_CATALOG + "/" + catalogId + "/watches/" + w.getWatchId())
                    .exchange()
                    // ← now expect 204 instead of 200
                    .expectStatus().isNoContent();
        }

        // Step 2: Add initial watch
        WatchRequestModel req = new WatchRequestModel();
        WatchBrand orig = new WatchBrand();
        orig.setBrandName("Seiko");
        orig.setBrandCountry("Japan");
        req.setWatchBrand(orig);

        Price pp = new Price();
        pp.setMsrp(BigDecimal.valueOf(300.00));
        pp.setCost(BigDecimal.valueOf(250.00));
        pp.setTotalOptionsCost(BigDecimal.ZERO);
        req.setPrice(pp);

        req.setUsageType(UsageType.NEW);
        req.setQuantity(7);
        req.setModel("5X47");
        req.setMaterial("Titanium");
        req.setAccessories(new ArrayList<>()); // Important fix for NPE

        WatchResponseModel added = webClient.post()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();

        // Step 3: Update that watch
        WatchRequestModel upd = new WatchRequestModel();
        WatchBrand updatedBrand = new WatchBrand();
        updatedBrand.setBrandName("SeikoUpdated");
        updatedBrand.setBrandCountry("JapanUpdated");
        upd.setWatchBrand(updatedBrand);

        Price p2 = new Price();
        p2.setMsrp(BigDecimal.valueOf(350.00));
        p2.setCost(BigDecimal.valueOf(300.00));
        p2.setTotalOptionsCost(BigDecimal.ZERO);
        upd.setPrice(p2);

        upd.setUsageType(UsageType.USED);
        upd.setQuantity(3);
        upd.setModel("5X47U");
        upd.setMaterial("Ceramic");
        upd.setAccessories(new ArrayList<>()); // Important fix for NPE

        webClient.put()
                .uri(BASE_CATALOG + "/" + catalogId + "/watches/" + added.getWatchId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isOk()  // stays 200 OK
                .expectBody(WatchResponseModel.class)
                .value(resp -> {
                    assertEquals("SeikoUpdated", resp.getWatchBrand().getBrandName());
                    assertEquals("JapanUpdated", resp.getWatchBrand().getBrandCountry());
                    assertEquals("5X47U",        resp.getModel());
                    assertEquals("Ceramic",      resp.getMaterial());
                });
    }
    // ─── Negative: update non‐existent watch returns 404 Not Found ──────────
    @Test
    public void updateWatch_nonExisting_returns404() {
        WatchRequestModel upd = new WatchRequestModel();
        WatchBrand b = new WatchBrand();
        b.setBrandName("X");
        b.setBrandCountry("Y");
        upd.setWatchBrand(b);

        Price p = new Price();
        p.setMsrp(BigDecimal.valueOf(1.00));
        p.setCost(BigDecimal.valueOf(1.00));
        p.setTotalOptionsCost(BigDecimal.ZERO);
        upd.setPrice(p);

        upd.setUsageType(UsageType.USED);
        upd.setQuantity(0);
        upd.setModel("X1");
        upd.setMaterial("M");

        webClient.put()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches/no-such")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("unknown watch id"),
                            "Expected error message to contain 'unknown watch id'");
                });
    }



    // ─── Negative: delete non‐existent watch returns 404 Not Found ──────────
    @Test
    public void deleteWatch_nonExisting_returns404() {
        webClient.delete()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches/no-such")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("unknown watch id"));
                });
    }

    // ─── Negative: add with missing nested WatchBrand returns 400 Bad Request ─
    @Test
    public void addWatch_invalidMissingFields_returnsInternalServerError() {
        WatchRequestModel bad = new WatchRequestModel(); // no watchBrand or price set

        webClient.post()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bad)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─── Direct exception‐handler test for InvalidInputException ─────────────
    @Test
    public void handleInvalidInputException_directly() {
        InvalidInputException ex = new InvalidInputException("bad watch");
        HttpErrorInfo info = exceptionHandler.handleInvalidInputException(
                mockWebRequest, ex
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, info.getHttpStatus());
        assertEquals("bad watch", info.getMessage());
        assertEquals("uri=/dummy/path", info.getPath());
    }

    // ─── Application startup sanity check ──────────────────────────────────
    @Test
    public void main_startsWithoutException() {
        assertDoesNotThrow(() -> ProductServicesApplication.main(
                new String[]{"--spring.main.web-application-type=none"}
        ));
    }


    @Test
    @DisplayName("Positive-POST /api/v1/catalogs → 201 CREATED when catalog type is new")
    public void createCatalog_uniqueType_returnsCreated() {
        // 1) Fetch & delete any seeded catalogs so identity will auto-increment cleanly
        List<CatalogResponseModel> seeded = webClient.get()
                .uri(BASE_CATALOG)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CatalogResponseModel.class)
                .returnResult()
                .getResponseBody();
        assertNotNull(seeded);
        String existingType = seeded.get(0).getType();
        for (CatalogResponseModel cat : seeded) {
            webClient.delete()
                    .uri(BASE_CATALOG + "/" + cat.getCatalogId())
                    .exchange()
                    .expectStatus().isNoContent();
        }

        // 2) Now post a truly new catalog
        String newType = existingType + "-NEW";
        CatalogRequestModel req = new CatalogRequestModel();
        req.setType(newType);
        req.setDescription("Some description");

        webClient.post()
                .uri(BASE_CATALOG)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CatalogResponseModel.class)
                .value(c -> {
                    assertNotNull(c.getCatalogId());
                    assertEquals(newType, c.getType());
                });
    }

    @Test
    @DisplayName("Negative-POST /api/v1/catalogs → 422 UNPROCESSABLE_ENTITY when catalog type exists")
    public void createCatalog_duplicateType_returnsUnprocessableEntity() {
        // grab a seeded type
        List<CatalogResponseModel> cats = webClient.get()
                .uri(BASE_CATALOG)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CatalogResponseModel.class)
                .returnResult()
                .getResponseBody();
        String dupType = cats.get(0).getType();

        CatalogRequestModel req = new CatalogRequestModel();
        req.setType(dupType);

        webClient.post()
                .uri(BASE_CATALOG)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(err -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, err.getHttpStatus());
                    assertEquals("Catalog with type " + dupType + " already exists.", err.getMessage());
                    assertEquals("uri=/api/v1/catalogs", err.getPath());
                });
    }

    @Test
    @DisplayName("Positive-POST /api/v1/catalogs/{id}/watches → 201 CREATED when watch model is new")
    public void addWatch_uniqueModel_returnsCreated() {
        // remove the one seed watch so we can insert a second
        webClient.delete()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches/WCH-001")
                .exchange()
                .expectStatus().isNoContent();

        String newModel = "UniqueModel-" + UUID.randomUUID();

        WatchRequestModel req = new WatchRequestModel();
        WatchBrand b = new WatchBrand();
        b.setBrandName("TestBrand");
        b.setBrandCountry("TestCountry");
        req.setWatchBrand(b);

        Price price = new Price();
        price.setMsrp(BigDecimal.valueOf(100));
        price.setCost(BigDecimal.valueOf(80));
        price.setTotalOptionsCost(BigDecimal.ZERO);
        req.setPrice(price);

        req.setUsageType(UsageType.NEW);
        req.setQuantity(1);
        req.setModel(newModel);
        req.setMaterial("Steel");

        webClient.post()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WatchResponseModel.class)
                .value(w -> {
                    assertNotNull(w.getWatchId());
                    assertEquals(newModel, w.getModel());
                });
    }

    @Test
    @DisplayName("Negative Post /api/v1/catalogs/{id}/watches → 422 UNPROCESSABLE_ENTITY when watch model exists")
    public void addWatch_duplicateModel_returnsUnprocessableEntity() {
        // pull the seeded watch’s model
        List<WatchResponseModel> existing = webClient.get()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WatchResponseModel.class)
                .returnResult()
                .getResponseBody();
        String dupModel = existing.get(0).getModel();

        WatchRequestModel req = new WatchRequestModel();
        // brand + price are irrelevant for exception
        WatchBrand b = new WatchBrand(); b.setBrandName("X"); b.setBrandCountry("Y");
        req.setWatchBrand(b);
        Price price = new Price();
        price.setMsrp(BigDecimal.ONE); price.setCost(BigDecimal.ONE);
        price.setTotalOptionsCost(BigDecimal.ZERO);
        req.setPrice(price);

        req.setUsageType(UsageType.NEW);
        req.setQuantity(1);
        req.setModel(dupModel);
        req.setMaterial("Any");

        webClient.post()
                .uri(BASE_CATALOG + "/" + VALID_CATALOG_ID + "/watches")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(err -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, err.getHttpStatus());
                    assertEquals("Watch with model " + dupModel + " already exists.", err.getMessage());
                    assertEquals(
                            "uri=/api/v1/catalogs/" + VALID_CATALOG_ID + "/watches",
                            err.getPath()
                    );
                });
    }













}
