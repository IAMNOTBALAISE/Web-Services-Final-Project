package com.example.customerservices.businesslayer;

import com.example.customerservices.businesslogiclayer.CustomerServiceImpl;
import com.example.customerservices.dataaccesslayer.*;
import com.example.customerservices.datamapperlayer.CustomerRequestMapper;
import com.example.customerservices.datamapperlayer.CustomerResponseMapper;
import com.example.customerservices.presentationlayer.CustomerRequestModel;
import com.example.customerservices.presentationlayer.CustomerResponseModel;
import com.example.customerservices.utils.exceptions.DuplicateCustomerEmailException;
import com.example.customerservices.utils.exceptions.InvalidInputException;
import com.example.customerservices.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository repo;

    @Spy
    private CustomerRequestMapper reqMapper = Mappers.getMapper(CustomerRequestMapper.class);

    @Spy
    private CustomerResponseMapper resMapper = Mappers.getMapper(CustomerResponseMapper.class);

    @InjectMocks
    private CustomerServiceImpl service;

    @BeforeEach
    public void resetMocks() {
        Mockito.reset(repo);
    }

    private Customer makeCustomer(String id, String email) {
        Customer c = new Customer();
        c.setCustomerIdentifier(new CustomerIdentifier(id));
        c.setFirstName("Fn");
        c.setLastName("Ln");
        c.setEmailAddress(email);
        Address addr = new Address("St", "PC", "City", "Prov");
        c.setCustomerAddress(addr);
        c.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.HOME, "123")));
        return c;
    }

    private CustomerRequestModel makeRequest(String email, String pw1, String pw2) {
        CustomerRequestModel m = new CustomerRequestModel();
        m.setFirstName("Fn");
        m.setLastName("Ln");
        m.setEmailAddress(email);
        m.setStreetAddress("St");
        m.setPostalCode("PC");
        m.setCity("City");
        m.setProvince("Prov");
        m.setUsername("u");
        m.setPassword1(pw1);
        m.setPassword2(pw2);
        m.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.MOBILE, "456")));
        return m;
    }

    // Testing retrieval of all customers returns a mapped list (positive case)
    @Test
    public void getCustomers_returnsMappedList() {
        Customer stored = makeCustomer("ID1", "a@b.com");
        when(repo.findAll()).thenReturn(List.of(stored));

        List<CustomerResponseModel> list = service.getCustomers();

        assertEquals(1, list.size());
        assertEquals("ID1", list.get(0).getCustomerId());
        verify(repo).findAll();
    }

    // Testing getCustomerbyCustomerId returns a model when it exists (positive case)
    @Test
    public void getById_existing() {
        Customer stored = makeCustomer("ID1", "x@y.com");
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID1"))
                .thenReturn(stored);

        CustomerResponseModel resp = service.getCustomerbyCustomerId("ID1");
        assertEquals("ID1", resp.getCustomerId());
    }


    // Testing getCustomerbyCustomerId throws when none found (negative case)
    @Test
    public void getById_notFound_throws() {
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID1"))
                .thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.getCustomerbyCustomerId("ID1"));
    }

    // Testing addCustomer allows null passwords (positive case)
    @Test
    public void addCustomer_nullPasswords_allowed() {
        CustomerRequestModel req = makeRequest("u@u.com", null, null);
        when(repo.findCustomerByEmailAddress("u@u.com")).thenReturn(null);
        when(repo.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponseModel resp = service.addCustomer(req);
        assertEquals("Fn", resp.getFirstName());
        verify(repo).save(any(Customer.class));
    }

    // Testing addCustomer rejects mismatched passwords (negative case)
    @Test
    public void addCustomer_passwordMismatch_throws() {
        CustomerRequestModel req = makeRequest("u@u.com", "a", "b");
        assertThrows(IllegalArgumentException.class,
                () -> service.addCustomer(req));
    }

    // Testing addCustomer rejects duplicate email (negative case)
    @Test
    public void addCustomer_duplicateEmail_throws() {
        Customer stored = makeCustomer("ID2", "dup@x.com");
        when(repo.findCustomerByEmailAddress("dup@x.com")).thenReturn(stored);

        CustomerRequestModel req = makeRequest("dup@x.com", "p", "p");
        assertThrows(DuplicateCustomerEmailException.class,
                () -> service.addCustomer(req));
    }


    // Testing addCustomer success path maps, saves and returns (positive case)
    @Test
    public void addCustomer_success_setsIdentifierAndPassword() {
        CustomerRequestModel req = makeRequest("new@x.com", "p", "p");
        when(repo.findCustomerByEmailAddress("new@x.com")).thenReturn(null);
        when(repo.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerResponseModel resp = service.addCustomer(req);
        assertEquals("Fn", resp.getFirstName());
        verify(reqMapper).requestModelToEntity(req);
        verify(repo).save(any(Customer.class));
        verify(resMapper).entityToResponseModel(any(Customer.class));
    }

    // Testing updateCustomer throws if not found (negative case)
    @Test
    public void updateCustomer_notFound_throws() {
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID3"))
                .thenReturn(null);
        CustomerRequestModel req = makeRequest("e@e.com", "p", "p");
        assertThrows(NotFoundException.class,
                () -> service.updateCustomer("ID3", req));
    }

    // Testing updateCustomer rejects mismatched passwords (negative case)
    @Test
    public void updateCustomer_passwordMismatch_throws() {
        Customer stored = makeCustomer("ID4", "old@x.com");
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID4"))
                .thenReturn(stored);
        CustomerRequestModel req = makeRequest("e@e.com", "a", "b");
        assertThrows(IllegalArgumentException.class,
                () -> service.updateCustomer("ID4", req));
    }

    // Testing updateCustomer rejects duplicate email on different customer (negative case)
    @Test
    public void updateCustomer_duplicateEmailDifferentCustomer_throws() {
        Customer stored = makeCustomer("ID5", "old@x.com");
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID5"))
                .thenReturn(stored);
        Customer other  = makeCustomer("ID6", "dup@x.com");
        when(repo.findCustomerByEmailAddress("dup@x.com"))
                .thenReturn(other);

        CustomerRequestModel req = makeRequest("dup@x.com", "p", "p");
        assertThrows(DuplicateCustomerEmailException.class,
                () -> service.updateCustomer("ID5", req));
    }

    // Testing updateCustomer success overwrites fields and saves (positive case)
    @Test
    public void updateCustomer_success_overwritesAndSaves() {
        Customer stored = makeCustomer("ID7", "old@x.com");
        stored.setId(88);
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID7"))
                .thenReturn(stored);
        when(repo.findCustomerByEmailAddress("new@x.com")).thenReturn(null);
        when(repo.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        CustomerRequestModel req = makeRequest("new@x.com", "p", "p");
        CustomerResponseModel resp = service.updateCustomer("ID7", req);

        assertEquals("Fn", resp.getFirstName());
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repo).save(captor.capture());
        Customer saved = captor.getValue();
        assertEquals(88, saved.getId());
        assertEquals("ID7", saved.getCustomerIdentifier().getCustomerId());
    }

    // Testing deleteCustomerbyCustomerId throws if not found (negative case)
    @Test
    public void deleteCustomer_notFound_throws() {
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID8"))
                .thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> service.deleteCustomerbyCustomerId("ID8"));
    }

    // Testing deleteCustomerbyCustomerId deletes and returns message (positive case)
    @Test
    public void deleteCustomer_existing_deletesAndReturnsMsg() {
        Customer stored = makeCustomer("ID9", "x@x.com");
        when(repo.findCustomerByCustomerIdentifier_CustomerId("ID9"))
                .thenReturn(stored);

        String msg = service.deleteCustomerbyCustomerId("ID9");
        assertTrue(msg.contains("deleted successfully"));
        verify(repo).delete(stored);
    }

    // Testing getCustomerbyEmail throws if not found (negative case)
    @Test
    public void getByEmail_notFound_throws() {
        when(repo.findCustomerByEmailAddress("none@x.com")).thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> service.getCustomerbyEmail("none@x.com"));
    }


    // Testing getCustomerbyEmail returns model when exists (positive case)
    @Test
    public void getByEmail_existing_returnsModel() {
        Customer stored = makeCustomer("ID10", "e@e.com");
        when(repo.findCustomerByEmailAddress("e@e.com"))
                .thenReturn(stored);

        CustomerResponseModel resp = service.getCustomerbyEmail("e@e.com");
        assertEquals("e@e.com", resp.getEmailAddress());
    }

    // ----- helper methods -----


    // Testing fromEntityToModel and list variant (positive case)
    @Test
    public void fromEntityToModel_and_listVariant_work() {
        Customer c = new Customer();
        c.setFirstName("A");
        c.setLastName("B");

        CustomerResponseModel m = service.fromEntityToModel(c);
        assertEquals("A", m.getFirstName());
        assertEquals("B", m.getLastName());

        List<CustomerResponseModel> list = service.fromEntityListToModelList(List.of(c, c));
        assertEquals(2, list.size());
    }

    // ─── Exception‐constructor coverage ───────────────────────────────────────────


    // Testing NotFoundException constructors (positive case)
    @Test
    public void notFoundException_constructors() {
        // no‑arg ctor
        NotFoundException ex1 = new NotFoundException();
        assertNull(ex1.getMessage());

        // message‑only ctor
        NotFoundException ex2 = new NotFoundException("not found");
        assertEquals("not found", ex2.getMessage());

        // cause‑only ctor
        Throwable cause = new IllegalStateException();
        NotFoundException ex3 = new NotFoundException(cause);
        assertSame(cause, ex3.getCause());

        // message+cause ctor
        NotFoundException ex4 = new NotFoundException("oops", cause);
        assertEquals("oops", ex4.getMessage());
        assertSame(cause, ex4.getCause());
    }

    // Testing InvalidInputException constructors (positive case)
    @Test
    public void invalidInputException_constructors() {
        // no‑arg ctor
        InvalidInputException ex1 = new InvalidInputException();
        assertNull(ex1.getMessage());

        // message‑only ctor
        InvalidInputException ex2 = new InvalidInputException("bad input");
        assertEquals("bad input", ex2.getMessage());

        // cause‑only ctor
        Throwable cause = new NumberFormatException();
        InvalidInputException ex3 = new InvalidInputException(cause);
        assertSame(cause, ex3.getCause());

        // message+cause ctor
        InvalidInputException ex4 = new InvalidInputException("err", cause);
        assertEquals("err", ex4.getMessage());
        assertSame(cause, ex4.getCause());
    }

    // ─── Mapper‐coverage tests ────────────────────────────────────────────────

    // Testing CustomerRequestMapper mappings (positive case)
    @Test
    public void requestMapper_mapsRequestModelToEntity() {
        // reuse your makeRequest helper (it already sets firstName, lastName, etc)
        CustomerRequestModel req = makeRequest("map@test.com", "pw", "pw");
        req.setStreetAddress("123 Maple");
        req.setPostalCode("H1H1H1");
        req.setCity("Montreal");
        req.setProvince("QC");
        List<PhoneNumber> phones = List.of(new PhoneNumber(PhoneType.HOME, "555-0001"));
        req.setPhoneNumbers(phones);

        Customer entity = reqMapper.requestModelToEntity(req);

        // id should be ignored
        assertNull(entity.getId());
        // address mappings
        assertEquals("123 Maple", entity.getCustomerAddress().getStreetAddress());
        assertEquals("H1H1H1",     entity.getCustomerAddress().getPostalCode());
        assertEquals("Montreal",   entity.getCustomerAddress().getCity());
        assertEquals("QC",         entity.getCustomerAddress().getProvince());
        // phoneList
        assertEquals(phones, entity.getPhoneNumbers());
    }

    // Testing requestMapper list conversion (positive case)
    @Test
    public void requestMapper_mapsListToEntityList() {
        CustomerRequestModel r1 = makeRequest("a@b.com", "pw", "pw");
        r1.setCity("C1");
        CustomerRequestModel r2 = makeRequest("c@d.com", "pw", "pw");
        r2.setCity("C2");

        List<Customer> out = reqMapper.requestModelListToEntityList(List.of(r1, r2));
        assertEquals(2, out.size());
        assertEquals("C1", out.get(0).getCustomerAddress().getCity());
        assertEquals("C2", out.get(1).getCustomerAddress().getCity());
    }

    // Testing CustomerResponseMapper mappings (positive case)
    @Test
    public void responseMapper_mapsEntityToResponseModel() {
        // reuse your makeCustomer helper (it sets identifier, name, email, address, phones)
        Customer in = makeCustomer("CID-123", "x@y.com");
        in.setCustomerAddress(new Address("456 Elm","Z2Z2Z2","Toronto","ON"));
        in.setPhoneNumbers(List.of(new PhoneNumber(PhoneType.MOBILE,"999-1234")));

        CustomerResponseModel out = resMapper.entityToResponseModel(in);

        assertEquals("CID-123", out.getCustomerId());
        assertEquals("456 Elm", out.getStreetAddress());
        assertEquals("Z2Z2Z2",  out.getPostalCode());
        assertEquals("Toronto", out.getCity());
        assertEquals("ON",      out.getProvince());
        assertEquals(1,         out.getPhoneNumbers().size());
        assertEquals("999-1234",out.getPhoneNumbers().get(0).getNumber());
    }

    // Testing responseMapper list conversion (positive case)
    @Test
    public void responseMapper_mapsListToResponseModelList() {
        Customer c1 = makeCustomer("ID1","a@a.com");
        Customer c2 = makeCustomer("ID2","b@b.com");

        List<CustomerResponseModel> list = resMapper.entityListToResponseModelList(List.of(c1, c2));
        assertEquals(2, list.size());
        assertEquals("ID1", list.get(0).getCustomerId());
        assertEquals("ID2", list.get(1).getCustomerId());
    }



}
