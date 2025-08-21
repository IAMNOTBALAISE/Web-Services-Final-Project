package com.example.customerservices.presentationlayer;


import com.example.customerservices.CustomerServicesApplication;
import com.example.customerservices.businesslogiclayer.CustomerService;
import com.example.customerservices.businesslogiclayer.CustomerServiceImpl;
import com.example.customerservices.dataaccesslayer.*;
import com.example.customerservices.datamapperlayer.CustomerRequestMapper;
import com.example.customerservices.datamapperlayer.CustomerRequestMapperImpl;
import com.example.customerservices.datamapperlayer.CustomerResponseMapper;
import com.example.customerservices.datamapperlayer.CustomerResponseMapperImpl;
import com.example.customerservices.utils.GlobalControllerExceptionHandler;
import com.example.customerservices.utils.HttpErrorInfo;
import com.example.customerservices.utils.ResourceNotFoundException;
import com.example.customerservices.utils.exceptions.DuplicateCustomerEmailException;
import com.example.customerservices.utils.exceptions.InvalidInputException;
import com.example.customerservices.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/schema-h2.sql", "/data-h2.sql"})
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CustomerControllerIntegrationTest {


    @Autowired
    private WebTestClient webClient;

    @Autowired
    private GlobalControllerExceptionHandler exceptionHandler;

    @MockBean
    private WebRequest mockWebRequest;

    @BeforeEach
    void setupMockWebRequest() {
        // GlobalControllerExceptionHandler uses request.getDescription(false) for the path
        when(mockWebRequest.getDescription(false)).thenReturn("uri=/dummy/path");
    }

    private static final String BASE      = "/api/v1/customers";
    private static final String VALID_ID  = "123e4567-e89b-12d3-a456-556642440000";
    private static final String OTHER_ID  = "223e4567-e89b-12d3-a456-556642440001";
    private static final String VALID_EMAIL = "john.smith@example.com";


    // Testing delete of an existing customer returns 200 OK (positive case)
    @Test
    public void deleteExistingCustomer() {
        webClient.delete().uri(BASE + "/" + VALID_ID).exchange()
                .expectStatus().isNoContent();
    }


    // Testing retrieval by ID returns the correct customer (positive case)
    @Test
    public void getById_returnsCustomer() {
        webClient.get().uri(BASE + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(response -> {
                    assertEquals(VALID_ID, response.getCustomerId());
                    assertEquals("John",   response.getFirstName());
                });
    }


    // Testing that getting all customers returns exactly two from seeded data (positive case)
    @Test
    public void getAll_returnsTwoCustomers() {
        List<CustomerResponseModel> list = webClient.get().uri(BASE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseModel.class)
                .returnResult().getResponseBody();

        Map<String, CustomerResponseModel> byId =
                list.stream().collect(Collectors.toMap(CustomerResponseModel::getCustomerId, c -> c));

        assertEquals(2, list.size());
        assertEquals("John",  byId.get(VALID_ID).getFirstName());
        assertEquals("Emily", byId.get(OTHER_ID).getFirstName());
    }

    // Testing creation of a valid customer returns the created model (positive case)
    @Test
    public void createValidCustomer() {
        CustomerRequestModel req = new CustomerRequestModel();
        req.setFirstName("New");
        req.setLastName("User");
        req.setEmailAddress("new.user@example.com");
        req.setStreetAddress("Street");
        req.setPostalCode("P1P1P1");
        req.setCity("City");
        req.setProvince("Prov");
        req.setUsername("u");
        req.setPassword1("pw");
        req.setPassword2("pw");
        req.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.HOME,"111")));

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                // ← updated to expect 201 Created
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .value(response -> assertEquals("New", response.getFirstName()));
    }

    // Testing update of an existing customer returns updated model (positive case)
    @Test
    public void updateExistingCustomer() {
        CustomerRequestModel upd = new CustomerRequestModel();
        upd.setFirstName("Upd");
        upd.setLastName("User");
        upd.setEmailAddress(VALID_EMAIL);
        upd.setStreetAddress("St");
        upd.setPostalCode("PC");
        upd.setCity("City");
        upd.setProvince("Prov");
        upd.setUsername("u");
        upd.setPassword1("pw");
        upd.setPassword2("pw");
        upd.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.MOBILE,"222")));

        webClient.put().uri(BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upd)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(response -> assertEquals("Upd", response.getFirstName()));
    }

    // Testing retrieval by email query param returns the correct customer (positive case)
    @Test
    public void getByEmail_returnsCustomer() {
        webClient.get().uri(uri -> uri.path(BASE).queryParam("email", VALID_EMAIL).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(response -> assertEquals(VALID_EMAIL, response.getEmailAddress()));
    }

    //util -- the first two are testing this 4. All three low-level microservices shall implement positive and negative path
    //controller-based integration tests specific to the sub-domain specific exception
    //implemented in I5.


    // Testing DuplicateCustomerEmailException handler produces 422 status (negative case)
    @Test
    public void handleDuplicateCustomerEmailException_directly() {
        DuplicateCustomerEmailException ex =
                new DuplicateCustomerEmailException("dup@x.com");
        HttpErrorInfo info =
                exceptionHandler.handleDuplicateCustomerEmailException(mockWebRequest, ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, info.getHttpStatus());
        assertTrue(info.getMessage().contains("dup@x.com"));
    }

    // Testing DuplicateCustomerEmailException constructors (positive case)
    @Test
    public void duplicateCustomerEmailException_constructors() {
        // no‑arg ctor
        DuplicateCustomerEmailException ex1 = new DuplicateCustomerEmailException();
        assertNull(ex1.getMessage());

        // cause‑only ctor
        Throwable cause = new RuntimeException("root cause");
        DuplicateCustomerEmailException ex2 = new DuplicateCustomerEmailException(cause);
        assertSame(cause, ex2.getCause());

        // message‑only ctor
        DuplicateCustomerEmailException ex3 = new DuplicateCustomerEmailException("dup@mail.com");
        assertEquals("Customer with email dup@mail.com already exists.", ex3.getMessage());

        // message+cause ctor
        DuplicateCustomerEmailException ex4 = new DuplicateCustomerEmailException("msg", cause);
        assertEquals("msg", ex4.getMessage());
        assertSame(cause, ex4.getCause());
    }



    // Testing NotFoundException handler produces 404 status (negative case)
    @Test
    public void handleNotFoundException_directly() {
        NotFoundException ex = new NotFoundException("no-id");
        HttpErrorInfo info  = exceptionHandler.handleNotFoundException(mockWebRequest, ex);

        assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
        assertEquals("uri=/dummy/path",    info.getPath());
        assertEquals("no-id",              info.getMessage());
    }

    // Testing InvalidInputException handler produces 422 status (negative case)
    @Test
    public void handleInvalidInputException_directly() {
        InvalidInputException ex = new InvalidInputException("bad payload");
        HttpErrorInfo info       = exceptionHandler.handleInvalidInputException(mockWebRequest, ex);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, info.getHttpStatus());
        assertEquals("bad payload",                  info.getMessage());
    }


    // Testing IllegalArgumentException handler produces 400 status (negative case)
    @Test
    public void handleIllegalArgumentException_directly() {
        IllegalArgumentException ex = new IllegalArgumentException("illegal!");
        HttpErrorInfo info          = exceptionHandler.handleIllegalArgumentException(mockWebRequest, ex);

        assertEquals(HttpStatus.BAD_REQUEST, info.getHttpStatus());
        assertEquals("illegal!",           info.getMessage());
    }

    // Testing update with mismatched passwords returns 400 Bad Request (negative case)
    @Test
    public void updateCustomer_passwordMismatch_returnsBadRequest() {
        CustomerRequestModel badReq = new CustomerRequestModel();
        badReq.setFirstName("Bad");
        badReq.setLastName("User");
        badReq.setEmailAddress(VALID_EMAIL);
        badReq.setStreetAddress("X");
        badReq.setPostalCode("P");
        badReq.setCity("C");
        badReq.setProvince("Pr");
        badReq.setUsername("u");
        badReq.setPassword1("pw1");
        badReq.setPassword2("pw2");  // mismatch
        badReq.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.MOBILE, "000")));

        webClient.put().uri(BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badReq)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.BAD_REQUEST, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("password"));
                });
    }

    // Testing GET by a non-existent ID returns 404 Not Found (negative case)
    @Test
    public void getById_notFound_returns404() {
        webClient.get().uri(BASE + "/non-existing-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("not found"));
                });
    }

    // Testing GET by a non-existent email returns 404 Not Found (negative case)
    @Test
    public void getByEmail_notFound_returns404() {
        webClient.get().uri(uri -> uri.path(BASE)
                        .queryParam("email", "none@x.com")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("not found"));
                });
    }

    // Testing creation of a customer with an email that already exists returns 422 Unprocessable Entity (negative case)
    // Dupplication email exception being test here
    @Test
    public void createDuplicateEmail_returns422() {
        CustomerRequestModel dup = new CustomerRequestModel();
        dup.setFirstName("Dup");
        dup.setLastName("User");
        dup.setEmailAddress(VALID_EMAIL);  // already in seeded data
        dup.setStreetAddress("S");
        dup.setPostalCode("P");
        dup.setCity("C");
        dup.setProvince("Pr");
        dup.setUsername("u");
        dup.setPassword1("pw");
        dup.setPassword2("pw");
        dup.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.HOME, "111")));

        webClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dup)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, info.getHttpStatus());
                    // The controller message ends with "already exists."
                    assertTrue(info.getMessage().toLowerCase().contains("already exists"));
                });
    }

    // Testing deletion of a non-existent customer returns 404 Not Found (negative case)
    @Test
    public void deleteNonExistingCustomer_returns404() {
        webClient.delete().uri(BASE + "/does-not-exist")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(HttpErrorInfo.class)
                .value(info -> {
                    assertEquals(HttpStatus.NOT_FOUND, info.getHttpStatus());
                    assertTrue(info.getMessage().toLowerCase().contains("not found"));
                });
    }

    // Testing ResourceNotFoundException static factory creates correct message (positive case)
    @Test
    public void resourceNotFoundException_staticFactory() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        ResourceNotFoundException ex = ResourceNotFoundException.saleNotFound(id);

        assertTrue(ex.getMessage().endsWith(id.toString()));
    }

    // Testing application main starts without throwing an exception (positive case)
    @Test
    public void main_startsWithoutException() {
        assertDoesNotThrow(() ->
                CustomerServicesApplication.main(new String[]{"--spring.main.web-application-type=none"})
        );
    }


    // ─── domain/value‐object tests ────────────────────────────────────────────────

    // Testing Address all‑args and no‑args constructors along with equals/hashCode/toString (positive case)
    @Test
    public void address_allArgs_and_noArgs_and_equality_toString() {
        // all‑args ctor
        Address a1 = new Address("St", "P1", "City", "Prov");
        assertEquals("St", a1.getStreetAddress());
        a1.setCity("NewCity");
        assertEquals("NewCity", a1.getCity());

        Address a2 = new Address("St", "P1", "NewCity", "Prov");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertTrue(a1.toString().contains("NewCity"));

        // no‑args + setters
        Address a3 = new Address();
        a3.setStreetAddress("X");
        a3.setPostalCode("Z");
        a3.setCity("Y");
        a3.setProvince("W");
        assertEquals("Y", a3.getCity());
    }

    // Testing PhoneNumber getters, equals/hashCode and toString include type and number (positive case)
    @Test
    public void phoneNumber_hashCode_and_toString_work() {
        PhoneNumber p1 = new PhoneNumber(PhoneType.MOBILE, "222-2222");
        PhoneNumber p2 = new PhoneNumber(PhoneType.MOBILE, "222-2222");

        // getters
        assertEquals(PhoneType.MOBILE, p1.getType());
        assertEquals("222-2222",       p1.getNumber());

        // equals/hashCode
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());

        // toString contains key info
        String ts = p1.toString();
        assertTrue(ts.contains("PhoneNumber"));
        assertTrue(ts.contains("MOBILE"));
        assertTrue(ts.contains("222-2222"));
    }

    // Testing CustomerIdentifier default and all‑args constructors plus setter/getter (positive case)
    @Test
    public void customerIdentifier_default_and_allArgs_and_setter_getter() {
        // all‑args ctor + setter/getter
        CustomerIdentifier ci1 = new CustomerIdentifier("ID1");
        assertEquals("ID1", ci1.getCustomerId());
        ci1.setCustomerId("ID2");
        assertEquals("ID2", ci1.getCustomerId());

        // default ctor generates a valid UUID
        CustomerIdentifier ci2 = new CustomerIdentifier();
        String uuid = ci2.getCustomerId();
        assertNotNull(uuid);
        assertDoesNotThrow(() -> UUID.fromString(uuid));
    }

    // Testing CustomerRequestModel builder and property accessors (positive case)
    @Test
    public void customerRequestModel_builder_and_setters_getters() {
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("A")
                .lastName("B")
                .emailAddress("e@x.com")
                .streetAddress("St")
                .postalCode("P")
                .city("C")
                .province("Pr")
                .username("u")
                .password1("p1")
                .password2("p1")
                .phoneNumbers(List.of(new PhoneNumber(PhoneType.MOBILE, "789")))
                .build();

        assertEquals("A", req.getFirstName());
        req.setCity("NewC");
        assertEquals("NewC", req.getCity());
    }

    // Testing CustomerResponseModel setters and getters (positive case)
    @Test
    public void customerResponseModel_setters_getters() {
        CustomerResponseModel resp = new CustomerResponseModel();
        resp.setCustomerId("CID");
        resp.setFirstName("FN");
        resp.setCity("CityZ");

        assertEquals("CID",   resp.getCustomerId());
        assertEquals("FN",    resp.getFirstName());
        assertEquals("CityZ", resp.getCity());
    }

    // Testing Customer equals/hashCode and basic property accessors (positive case)
    @Test
    public void customer_equals_hashCode_and_accessors() {
        Address addr = new Address("123 Main","A1A1A1","City","Prov");
        List<PhoneNumber> phones = List.of(new PhoneNumber(PhoneType.HOME, "111-1111"));

        Customer c1 = new Customer(
                42,
                new CustomerIdentifier("CID"),
                "Doe",
                "Jane",
                "jane.doe@example.com",
                "jdoe",
                "secret",
                addr,
                phones
        );
        Customer c2 = new Customer(
                42,
                new CustomerIdentifier("CID"),
                "Doe",
                "Jane",
                "jane.doe@example.com",
                "jdoe",
                "secret",
                addr,
                phones
        );

        // equals/hashCode
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        // getters
        assertEquals("jdoe",  c1.getUsername());
        assertEquals("secret", c1.getPassword());
    }


}














