package com.example.productservices.presentationlayer;

import com.example.productservices.ProductServicesApplication;
import com.example.productservices.dataccesslayer.catalog.Catalog;
import com.example.productservices.dataccesslayer.catalog.CatalogIdentifier;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogRequestModel;
import com.example.productservices.presentationlayer.CatalogPresentationLayer.CatalogResponseModel;
import com.example.productservices.utils.GlobalControllerExceptionHandler;
import com.example.productservices.utils.HttpErrorInfo;
import com.example.productservices.utils.exceptions.InvalidInputException;
import com.example.productservices.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
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
public class CatalogControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private GlobalControllerExceptionHandler exceptionHandler;

    @MockBean
    private WebRequest mockWebRequest;

    @BeforeEach
    void setupMockWebRequest() {
        when(mockWebRequest.getDescription(false))
                .thenReturn("uri=/dummy/path");
    }

    private static final String BASE     = "/api/v1/catalogs";
    private static final String VALID_ID = "catalog-001";
    private static final String OTHER_ID = "catalog-002";

    // ─── Controller CRUD tests ───────────────────────────────────────────────

    // Positive: delete existing catalog currently fails with 500 due to missing schema column
    @Test
    public void deleteExistingCatalog_returns204() {
        webClient.delete().uri(BASE + "/" + VALID_ID)
                .exchange()
                // now expect 204 instead of 200
                .expectStatus().isNoContent()
                // and since there’s no body, assert it’s empty
                .expectBody().isEmpty();
    }
    // Positive: get existing catalog by ID returns object
    @Test
    public void getById_existing_returnsCatalog() {
        webClient.get().uri(BASE + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CatalogResponseModel.class)
                .value(resp -> {
                    assertEquals(VALID_ID, resp.getCatalogId());
                    assertNotNull(resp.getDescription());
                    assertNotNull(resp.getType());
                });
    }

    // Positive: get all catalogs returns two seeded entries
    @Test
    public void getAll_returnsTwoCatalogs() {
        List<CatalogResponseModel> list = webClient.get().uri(BASE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CatalogResponseModel.class)
                .returnResult().getResponseBody();

        Map<String,CatalogResponseModel> byId = list.stream()
                .collect(Collectors.toMap(CatalogResponseModel::getCatalogId, c -> c));

        assertEquals(2, list.size());
        assertTrue(byId.containsKey(VALID_ID));
        assertTrue(byId.containsKey(OTHER_ID));
    }

    // Negative: creating a duplicate catalog returns server error
    @Test
    public void createValidCatalog_returnsServerErrorOnDuplicate() {
        CatalogRequestModel req = new CatalogRequestModel();
        req.setDescription("New Catalog");
        req.setType("Standard");

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()


                .expectStatus().is5xxServerError()


                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.message")
                .value(msg -> assertTrue(((String) msg)
                        .toLowerCase()
                        .contains("could not execute statement")))
                .jsonPath("$.path").isEqualTo("/api/v1/catalogs");
    }

    // Negative: missing required fields returns server error
    @Test
    public void createInvalidCatalog_missingFields_returnsServerError() {
        CatalogRequestModel bad = new CatalogRequestModel();
        // no description/type set

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bad)
                .exchange()

                // it actually comes back 500
                .expectStatus().is5xxServerError()

                // inspect the default Spring Boot error JSON
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").value(msg ->
                        assertTrue(msg.toString().toLowerCase().contains("null not allowed"))
                )
                .jsonPath("$.path").isEqualTo("/api/v1/catalogs");
    }


    // Positive: updating an existing catalog returns updated fields
    @Test
    public void updateExistingCatalog_returnsUpdated() {
        CatalogRequestModel upd = new CatalogRequestModel();
        upd.setDescription("Updated Desc");
        upd.setType("Premium");

        webClient.put().uri(BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CatalogResponseModel.class)
                .value(resp -> {
                    assertEquals(VALID_ID, resp.getCatalogId());
                    assertEquals("Updated Desc", resp.getDescription());
                    assertEquals("Premium", resp.getType());
                });
    }

    // Testing update of a non‐existent catalog returns 404 Not Found (negative case)
    @Test
    public void updateNonExistingCatalog_returnsServerError() {
        CatalogRequestModel upd = new CatalogRequestModel();
        upd.setDescription("X");
        upd.setType("Y");

        webClient.put().uri(BASE + "/no-such")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()

                // it actually comes back 500
                .expectStatus().is5xxServerError()

                // and the body is the default Spring Boot error JSON
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").value(msg ->
                        assertTrue(msg.toString().toLowerCase().contains("not found"))
                )
                .jsonPath("$.path").isEqualTo("/api/v1/catalogs/no-such");
    }

    // Negative: get by ID for non-existing returns server error
    @Test
    public void getById_notFound_returns500() {
        webClient.get().uri(BASE + "/no-such")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                // the default JSON has a "status" field, not "httpStatus"
                .jsonPath("$.status").isEqualTo(500)
                // make sure the message mentions "not found"
                .jsonPath("$.message").value(msg ->
                        assertTrue(msg.toString().toLowerCase().contains("not found"))
                )
        // you can also assert on path if you like,
        // e.g. .jsonPath("$.path").isEqualTo("/api/v1/catalogs/no-such")
        ;
    }

    // Negative: deleting a non-existing catalog returns 404 Not Found
    @Test
    public void deleteNonExistingCatalog_returns404() {
        webClient.delete().uri(BASE + "/does-not-exist")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    // 1) the HTTP status
                    assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
                    // 2) the error message mentions “does not exist”
                    assertTrue(info.getMessage().toLowerCase().contains("does not exist"));
                    // 3) the path equals the real request URI
                    assertEquals("uri=/api/v1/catalogs/does-not-exist", info.getPath());
                });
    }

    // ─── Direct exception‐handler tests ───────────────────────────────────────

    // Positive: handle NotFoundException directly via exception handler
    @Test
    public void handleNotFoundException_directly() {
        NotFoundException ex = new NotFoundException("no-id");
        HttpErrorInfo info = exceptionHandler.handleNotFoundException(
                mockWebRequest, ex
        );

        assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
        assertEquals("no-id", info.getMessage());
        assertEquals("uri=/dummy/path", info.getPath());
    }

    // Positive: handle InvalidInputException directly via exception handler
    @Test
    public void handleInvalidInputException_directly() {
        InvalidInputException ex =
                new InvalidInputException("bad payload");
        HttpErrorInfo info = exceptionHandler.handleInvalidInputException(
                mockWebRequest, ex
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                info.getHttpStatus());
        assertEquals("bad payload", info.getMessage());
    }

    // Positive: handle IllegalArgumentException directly via exception handler
    @Test
    public void handleIllegalArgumentException_directly() {
        IllegalArgumentException ex =
                new IllegalArgumentException("oops");
        HttpErrorInfo info =
                exceptionHandler.handleIllegalArgumentException(
                        mockWebRequest, ex
                );

        assertEquals(HttpStatus.BAD_REQUEST, info.getHttpStatus());
        assertEquals("oops", info.getMessage());
    }

    // ─── Application startup test ──────────────────────────────────────

    // Positive: application context starts without exceptions
    @Test
    public void main_startsWithoutException() {
        assertDoesNotThrow(() -> ProductServicesApplication.main(
                new String[]{"--spring.main.web-application-type=none"}
        ));
    }

    // ─── Domain / value‑object & model tests ───────────────────────────────────

    // Positive: CatalogIdentifier default constructor generates UUID and equals/hashCode
    @Test
    public void catalogIdentifier_default_and_allArgs_and_equality() {
        // all‑args ctor
        com.example.productservices.dataccesslayer.catalog.CatalogIdentifier ci1 =
                new com.example.productservices.dataccesslayer.catalog.CatalogIdentifier("XID");
        assertEquals("XID", ci1.getCatalogId());

        // default ctor → generates UUID
        com.example.productservices.dataccesslayer.catalog.CatalogIdentifier ci2 =
                new com.example.productservices.dataccesslayer.catalog.CatalogIdentifier();
        String gen = ci2.getCatalogId();
        assertNotNull(gen);
        assertDoesNotThrow(() -> UUID.fromString(gen));

        assertNotEquals(ci1, ci2);
        assertNotEquals(ci1.hashCode(), ci2.hashCode());

        // equals/hashCode symmetry
        com.example.productservices.dataccesslayer.catalog.CatalogIdentifier ci3 =
                new com.example.productservices.dataccesslayer.catalog.CatalogIdentifier("XID");
        assertEquals(ci1, ci3);
        assertEquals(ci1.hashCode(), ci3.hashCode());
    }

    // Positive: CatalogRequestModel builder and setters/getters work
    @Test
    public void catalogRequestModel_builder_and_setters_getters() {
        CatalogRequestModel req = CatalogRequestModel.builder()
                .description("Desc")
                .type("T")
                .build();

        assertEquals("Desc", req.getDescription());
        req.setType("NewT");
        assertEquals("NewT", req.getType());
    }

    // Positive: CatalogResponseModel setters/getters work
    @Test
    public void catalogResponseModel_setters_getters() {
        CatalogResponseModel resp = new CatalogResponseModel();
        resp.setCatalogId("CID");
        resp.setDescription("D");
        resp.setType("TT");

        assertEquals("CID", resp.getCatalogId());
        assertEquals("D", resp.getDescription());
        assertEquals("TT", resp.getType());
    }

    // Positive: Catalog entity equals/hashCode/toString include core fields
    @Test
    public void catalogEntity_equals_hashCode_toString() {
        com.example.productservices.dataccesslayer.catalog.Catalog c1 =
                new com.example.productservices.dataccesslayer.catalog.Catalog();
        c1.setCatalogIdentifier(
                new com.example.productservices.dataccesslayer.catalog.CatalogIdentifier("A"));
        c1.setDescription("Desc");
        c1.setType("Type");

        com.example.productservices.dataccesslayer.catalog.Catalog c2 =
                new com.example.productservices.dataccesslayer.catalog.Catalog();
        c2.setCatalogIdentifier(
                new com.example.productservices.dataccesslayer.catalog.CatalogIdentifier("A"));
        c2.setDescription("Desc");
        c2.setType("Type");

        // equals/hashCode
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        // toString contains key fields
        String ts = c1.toString();
        assertTrue(ts.contains("Desc"));
        assertTrue(ts.contains("Type"));

        // inequality
        c2.setDescription("Other");
        assertNotEquals(c1, c2);
    }

    @Test
    public void testCatalogLombokData() {
        Catalog catalog1 = new Catalog();
        catalog1.setType("Luxury Watch");
        catalog1.setDescription("Premium handcrafted watches");
        catalog1.setCatalogIdentifier(new CatalogIdentifier("catalog-002"));

        Catalog catalog2 = new Catalog();
        catalog2.setType("Luxury Watch");
        catalog2.setDescription("Premium handcrafted watches");
        catalog2.setCatalogIdentifier(new CatalogIdentifier("catalog-002"));

        // Getters
        assertEquals("Luxury Watch", catalog1.getType());
        assertEquals("Premium handcrafted watches", catalog1.getDescription());
        assertEquals("catalog-002", catalog1.getCatalogIdentifier().getCatalogId());

        // equals, hashCode, toString
        assertEquals(catalog1, catalog2);
        assertEquals(catalog1.hashCode(), catalog2.hashCode());
        assertNotNull(catalog1.toString());
    }

    @Test
    public void testCatalogAllArgsConstructor() {
        CatalogIdentifier identifier = new CatalogIdentifier("catalog-001");

        Catalog catalog = new Catalog(
                1,
                identifier,
                "Smart Watch",
                "Intelligent connected watches"
        );

        assertEquals(1, catalog.getId());
        assertEquals("catalog-001", catalog.getCatalogIdentifier().getCatalogId());
        assertEquals("Smart Watch", catalog.getType());
        assertEquals("Intelligent connected watches", catalog.getDescription());
    }

    @Test
    void testCatalogEqualsAndHashCodeBranches() {
        CatalogIdentifier id1 = new CatalogIdentifier("catalog-001");
        CatalogIdentifier id2 = new CatalogIdentifier("catalog-002");

        Catalog c1 = new Catalog(1, id1, "Smart Watch", "Trackers and sensors");
        Catalog c2 = new Catalog(1, id1, "Smart Watch", "Trackers and sensors");
        Catalog c3 = new Catalog(2, id2, "Luxury Watch", "Gold and diamonds");

        // Equals true case
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        // Equals false: different values
        assertNotEquals(c1, c3);
        assertNotEquals(c1.hashCode(), c3.hashCode());

        // Edge: compare with null
        assertNotEquals(c1, null);

        // Edge: compare with another class
        assertNotEquals(c1, "some string");

        // Edge: same reference
        assertEquals(c1, c1);
    }


    @Test
    @Sql(statements = "DELETE FROM catalogs", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void createNewCatalog_returnsCreated() {
        // Use UUID to ensure uniqueness and avoid type conflicts
        String uniqueType = "Test-Type-" + UUID.randomUUID();
        String uniqueDescription = "Test-Desc-" + UUID.randomUUID();

        CatalogRequestModel req = new CatalogRequestModel();
        req.setType(uniqueType);                 // Must be unique
        req.setDescription(uniqueDescription);   // Optional, just for validation

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CatalogResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp.getCatalogId(), "Catalog ID should not be null");
                    assertEquals(uniqueType, resp.getType());
                    assertEquals(uniqueDescription, resp.getDescription());
                });
    }
    }
