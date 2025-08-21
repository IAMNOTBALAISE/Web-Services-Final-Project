package com.example.serviceplanservices.presentationlayer;


import com.example.serviceplanservices.utils.GlobalControllerExceptionHandler;
import com.example.serviceplanservices.utils.HttpErrorInfo;
import com.example.serviceplanservices.utils.exceptions.DuplicateCoverageDetailsException;
import com.example.serviceplanservices.utils.exceptions.InvalidInputException;
import com.example.serviceplanservices.utils.exceptions.NotFoundException;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/schema-h2.sql", "/data-h2.sql"})
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PlanControllerIntegrationTest {


    @Autowired
    private WebTestClient webClient;

    @Autowired
    private GlobalControllerExceptionHandler handler;

    @MockBean
    private WebRequest webRequest;



    private static final String BASE         = "/api/v1/plans";
    private static final String VALID_ID     = "SP-001";
    private static final String OTHER_ID     = "SP-002";
    private static final String VALID_COVER  = "Full coverage for 1 year";

    // ─── GET all (positive) ───────────────────────────────────────────────────────
    @Test
    void getAll_returnsTwoPlans() {
        List<ServicePlanResponseModel> list = webClient.get().uri(BASE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ServicePlanResponseModel.class)
                .returnResult()
                .getResponseBody();

        assertThat(list).hasSize(2);

        Map<String, ServicePlanResponseModel> byId =
                list.stream().collect(Collectors.toMap(ServicePlanResponseModel::getPlanId, p->p));

        assertThat(byId.get(VALID_ID).getCoverageDetails()).isEqualTo(VALID_COVER);
        assertThat(byId.get(OTHER_ID).getCoverageDetails()).contains("Extended warranty");
    }

    // ─── GET by ID (positive) ─────────────────────────────────────────────────────
    @Test
    void getById_returnsPlan() {
        webClient.get().uri(BASE + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> {
                    assertThat(p.getPlanId()).isEqualTo(VALID_ID);
                    assertThat(p.getCoverageDetails()).isEqualTo(VALID_COVER);
                });
    }

    // ─── GET by ID (negative) ─────────────────────────────────────────────────────
    @Test
    void getById_notFound_returns404() {
        webClient.get().uri(BASE + "/NOPE")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertThat(info.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(info.getMessage().toLowerCase()).contains("not found");
                });
    }

    // ─── POST new plan (positive) ─────────────────────────────────────────────────
    @Test
    void createPlan_returnsCreated() {
        ServicePlanRequestModel req = new ServicePlanRequestModel("MyCov", LocalDate.of(2029, 9, 9));

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                // now expect 201 Created instead of 200 OK
                .expectStatus().isCreated()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> {
                    assertThat(p.getPlanId()).isNotBlank();
                    assertThat(p.getCoverageDetails()).isEqualTo("MyCov");
                });
    }

    // ─── PUT update existing (positive) ───────────────────────────────────────────
    @Test
    void updatePlan_returnsUpdated() {
        ServicePlanRequestModel upd = new ServicePlanRequestModel("XCov", LocalDate.of(2030,1,1));

        webClient.put().uri(BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> assertThat(p.getCoverageDetails()).isEqualTo("XCov"));
    }

    // ─── DELETE existing (positive) ───────────────────────────────────────────────
    @Test
    void deletePlan_returnsOk() {
        webClient.delete().uri(BASE + "/" + OTHER_ID)
                .exchange()
                .expectStatus().isNoContent();
//                .expectBody(String.class)
//                .value(msg -> assertThat(msg.toLowerCase()).contains("deleted"));
    }

    // ─── DELETE non‑existent (negative) ──────────────────────────────────────────
    @Test
    void deletePlan_notFound_returns404() {
        webClient.delete().uri(BASE + "/NOPE")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info ->
                        assertThat(info.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND)
                );
    }

    //request model and response

    // Positive: ResponseModel all‑args ctor, getters, setters and builder
    @Test
    void responseModel_allArgsAndNoArgs_andAccessors() {
        LocalDate exp = LocalDate.of(2030,12,31);
        ServicePlanResponseModel filled =
                new ServicePlanResponseModel("ID123","Cov",exp);

        assertEquals("ID123", filled.getPlanId());
        assertEquals("Cov",   filled.getCoverageDetails());
        assertEquals(exp,     filled.getExpirationDate());

        // via setters (still positive)
        filled.setPlanId("NEWID");
        filled.setCoverageDetails("NEWCOV");
        LocalDate later = exp.plusDays(1);
        filled.setExpirationDate(later);

        assertEquals("NEWID", filled.getPlanId());
        assertEquals("NEWCOV",filled.getCoverageDetails());
        assertEquals(later,   filled.getExpirationDate());

        // via builder (still positive)
        ServicePlanResponseModel b = ServicePlanResponseModel.builder()
                .planId("BID")
                .coverageDetails("BCOV")
                .expirationDate(exp)
                .build();
        assertEquals("BID",  b.getPlanId());
        assertEquals("BCOV", b.getCoverageDetails());
        assertEquals(exp,    b.getExpirationDate());
    }

    // Positive: ResponseModel equals, hashCode, toString, reflexive / null / wrong‑type
    // ─── ResponseModel: identity equals/hashCode/reflexive/null/wrong‑type (negative) ─────────
    @Test
    void responseModel_identityEquals_andHashCode_reflexiveAndNegative() {
        LocalDate d = LocalDate.of(2025, 1, 1);
        ServicePlanResponseModel m1 = new ServicePlanResponseModel("X", "C", d);
        ServicePlanResponseModel m2 = new ServicePlanResponseModel("X", "C", d);

        // reflexive
        assertEquals(m1, m1, "An object must equal itself");

        // two distinct instances should not be equal (default identity equals)
        assertNotEquals(m1, m2, "Separate instances should not be equal");

        // null and wrong‑type
        assertNotEquals(m1, null, "Should not equal null");
        assertNotEquals(m1, "foo", "Should not equal an unrelated type");

        // hashCode stable on same instance
        assertEquals(m1.hashCode(), m1.hashCode(), "hashCode must be consistent");
    }

    // Positive: RequestModel all‑args ctor, getters, setters and builder
    @Test
    void requestModel_allArgsAndNoArgs_andAccessors() {
        LocalDate exp = LocalDate.of(2040,6,30);
        ServicePlanRequestModel all = new ServicePlanRequestModel("RCov", exp);

        assertEquals("RCov", all.getCoverageDetails());
        assertEquals(exp,    all.getExpirationDate());

        // via setters (still positive)
        all.setCoverageDetails("RCT");
        all.setExpirationDate(exp.plusDays(5));
        assertEquals("RCT",             all.getCoverageDetails());
        assertEquals(exp.plusDays(5),   all.getExpirationDate());

        // via builder (still positive)
        ServicePlanRequestModel b = ServicePlanRequestModel.builder()
                .coverageDetails("BB")
                .expirationDate(exp)
                .build();
        assertEquals("BB", b.getCoverageDetails());
        assertEquals(exp,  b.getExpirationDate());
    }

    // ─── RequestModel: identity equals/hashCode/reflexive/null/wrong‑type (negative) ─────────
    @Test
    void requestModel_identityEquals_andHashCode_reflexiveAndNegative() {
        LocalDate d = LocalDate.of(2023, 3, 3);
        ServicePlanRequestModel r1 = new ServicePlanRequestModel("Z", d);
        ServicePlanRequestModel r2 = new ServicePlanRequestModel("Z", d);

        // reflexive
        assertEquals(r1, r1, "An object must equal itself");

        // two distinct instances with same data are NOT equal under default identity equals
        assertNotEquals(r1, r2, "Separate instances should not be equal");

        // null and wrong‑type
        assertNotEquals(r1, null, "Should not equal null");
        assertNotEquals(r1, "foo", "Should not equal an unrelated type");

        // hashCode is stable on the same instance
        assertEquals(r1.hashCode(), r1.hashCode(), "hashCode must remain consistent");
    }


    //gloal exception handler

    // Positive: handle NotFoundException → 404, correct path/message
    @Test
    void handleNotFoundException_returns404AndPathAndMessage() {
        NotFoundException ex = new NotFoundException("no-id");
        HttpErrorInfo info = handler.handleNotFoundException(webRequest, ex);

        assertThat(info.getHttpStatus())
                .isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(info.getPath()).isEqualTo("uri=/dummy/path");
        assertThat(info.getMessage()).isEqualTo("no-id");
    }

    // Positive: handle InvalidInputException → 422
    @Test
    void handleInvalidInputException_returns422() {
        InvalidInputException ex = new InvalidInputException("bad-payload");
        HttpErrorInfo info = handler.handleInvalidInputException(webRequest, ex);

        assertThat(info.getHttpStatus())
                .isEqualTo(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(info.getMessage()).isEqualTo("bad-payload");
    }

    // Positive: handle DuplicateCoverageDetailsException → 422, message includes details
    @Test
    void handleDuplicateCoverageDetailsException_returns422() {
        DuplicateCoverageDetailsException ex =
                new DuplicateCoverageDetailsException("Gold Plan");
        HttpErrorInfo info = handler.handleDuplicateCoverageDetailsException(webRequest, ex);

        assertThat(info.getHttpStatus())
                .isEqualTo(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(info.getMessage())
                .contains("Gold Plan")
                .contains("already exists");
    }

    // Positive: handle IllegalArgumentException → 400
    @Test
    void handleIllegalArgumentException_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("oops");
        HttpErrorInfo info = handler.handleIllegalArgumentException(webRequest, ex);

        assertThat(info.getHttpStatus())
                .isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(info.getMessage()).isEqualTo("oops");
    }

    /// utils

    // ─── NotFoundException ────────────────────────────────────────────────────

    // Positive: no‑arg ctor leaves message & cause null
    @Test
    void notFound_noArgs_defaults() {
        NotFoundException ex = new NotFoundException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    // Positive: message‑only ctor sets message
    @Test
    void notFound_messageOnly_setsMessage() {
        NotFoundException ex = new NotFoundException("not found!");
        assertEquals("not found!", ex.getMessage());
        assertNull(ex.getCause());
    }

    // Positive: cause‑only constructor sets cause and message to cause.toString()
    @Test
    void notFound_causeOnly_preservesCauseAndMessageFromCause() {
        Throwable cause = new IllegalStateException("oops");
        NotFoundException ex = new NotFoundException(cause);

        // the cause must be preserved
        assertSame(cause, ex.getCause(), "Cause must be the same instance");

        // by default, Throwable(cause) sets the message to cause.toString()
        assertEquals(
                cause.toString(),
                ex.getMessage(),
                "Message should default to cause.toString()"
        );
    }

    // Positive: message+cause ctor sets both
    @Test
    void notFound_messageAndCause_setsBoth() {
        Throwable cause = new RuntimeException();
        NotFoundException ex = new NotFoundException("bad", cause);
        assertEquals("bad", ex.getMessage());
        assertSame(cause, ex.getCause());
    }


    // ─── InvalidInputException ────────────────────────────────────────────────

    // Positive: no‑arg ctor leaves message & cause null
    @Test
    void invalidInput_noArgs_defaults() {
        InvalidInputException ex = new InvalidInputException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
    }

    // Positive: message‑only ctor sets message
    @Test
    void invalidInput_messageOnly_setsMessage() {
        InvalidInputException ex = new InvalidInputException("bad input");
        assertEquals("bad input", ex.getMessage());
        assertNull(ex.getCause());
    }

    // Positive: cause-only constructor sets cause and message to cause.toString()
    @Test
    void invalidInput_causeOnly_setsCauseAndMessageFromCauseToString() {
        Throwable cause = new NumberFormatException("nf");
        InvalidInputException ex = new InvalidInputException(cause);

        // cause must be preserved
        assertSame(cause, ex.getCause(), "Cause must be the same instance");

        // by default, Throwable(cause) sets the message to cause.toString()
        assertEquals(
                cause.toString(),
                ex.getMessage(),
                "Message should default to cause.toString()"
        );
    }

    // Positive: message+cause ctor sets both
    @Test
    void invalidInput_messageAndCause_setsBoth() {
        Throwable cause = new RuntimeException();
        InvalidInputException ex = new InvalidInputException("err", cause);
        assertEquals("err", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("Negative Path: POST /api/v1/plans with unique coverage → 201 CREATED")
    void createPlan_uniqueCoverage_returnsCreated() {
        // use a coverage plan not present in data-h2.sql
        ServicePlanRequestModel req =
                new ServicePlanRequestModel("Unique coverage plan", LocalDate.now().plusYears(1));

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ServicePlanResponseModel.class)
                .value(p -> {
                    assertThat(p.getPlanId()).isNotBlank();
                    assertThat(p.getCoverageDetails())
                            .isEqualTo("Unique coverage plan");
                });
    }

    @Test
    @DisplayName("Positive Exception Path: POST /api/v1/plans with duplicate coverage → 422 UNPROCESSABLE_ENTITY")
    void createPlan_duplicateCoverage_returnsUnprocessableEntity() {
        // given a coverage already in data-h2.sql
        ServicePlanRequestModel req =
                new ServicePlanRequestModel(VALID_COVER, LocalDate.now().plusYears(1));

        // when / then
        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(err -> {
                    assertThat(err.getHttpStatus())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(err.getMessage())
                            .isEqualTo("Coverage details " + VALID_COVER + " already exists.");
                    assertThat(err.getPath())
                            .isEqualTo("uri=/api/v1/plans");
                });
    }
















    @BeforeEach
    void setupWebRequest() {
        when(webRequest.getDescription(false)).thenReturn("uri=/dummy/path");
    }

}
