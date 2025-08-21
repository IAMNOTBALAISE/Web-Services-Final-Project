package com.example.customerservices.dataaccesslayer;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CustomerRepositoryIntegrationTest {

    @Autowired
    private CustomerRepository repo;

    private static final String VALID_ID  = "123e4567-e89b-12d3-a456-556642440000";
    private static final String OTHER_ID  = "223e4567-e89b-12d3-a456-556642440001";
    private static final String SAMPLE_ID = "SAMPLE-123";


    // Testing that saving and then retrieving by ID returns the correct customer (positive case)
    @Test
    public void whenCustomerExists_ReturnCustomerById() {
        Customer c = new Customer();
        c.setCustomerIdentifier(new CustomerIdentifier(VALID_ID));
        c.setFirstName("John");
        repo.save(c);

        Customer found = repo.findCustomerByCustomerIdentifier_CustomerId(VALID_ID);
        assertNotNull(found);
        assertEquals(VALID_ID, found.getCustomerIdentifier().getCustomerId());
    }


    // Testing that deleting all and saving two customers returns exactly two (positive case)
    @Test
    public void whenCustomersExist_ReturnAllCustomers() {
        repo.deleteAll();

        Customer c1 = new Customer();
        c1.setCustomerIdentifier(new CustomerIdentifier(VALID_ID));
        Customer c2 = new Customer();
        c2.setCustomerIdentifier(new CustomerIdentifier(OTHER_ID));
        repo.saveAll(List.of(c1, c2));

        List<Customer> all = repo.findAll();
        assertEquals(2, all.size());
    }

    // Testing that saving a customer with an email lets us find by that email (positive case)
    @Test
    public void whenEmailExists_ReturnCustomer() {
        Customer c = new Customer();
        c.setCustomerIdentifier(new CustomerIdentifier(VALID_ID));
        c.setEmailAddress("a@b.com");
        repo.save(c);

        Customer found = repo.findCustomerByEmailAddress("a@b.com");
        assertNotNull(found);
        assertEquals("a@b.com", found.getEmailAddress());
    }

    // Testing that existsByCustomerIdentifier returns true for a saved ID and false otherwise (positive/negative)
    @Test
    public void whenExistsByIdentifier_returnsTrueOtherwiseFalse() {
        Customer c = new Customer();
        c.setCustomerIdentifier(new CustomerIdentifier(VALID_ID));
        repo.save(c);
        assertTrue(repo.existsByCustomerIdentifier_CustomerId(VALID_ID));
        assertFalse(repo.existsByCustomerIdentifier_CustomerId("no-such"));
    }

    // Testing that updating a customer's firstname is persisted (positive case)
    @Test
    public void whenUpdateCustomer_thenFieldIsPersisted() {
        Customer c = new Customer();
        c.setCustomerIdentifier(new CustomerIdentifier(SAMPLE_ID));
        c.setFirstName("Foo");
        repo.save(c);

        c.setFirstName("Bar");
        repo.save(c);

        Customer reloaded = repo.findCustomerByCustomerIdentifier_CustomerId(SAMPLE_ID);
        assertEquals("Bar", reloaded.getFirstName());
    }

    // Testing that deleting one of two customers leaves exactly the other (positive case)
    @Test
    public void whenDeleteCustomer_thenGone() {
        Customer a = new Customer();
        a.setCustomerIdentifier(new CustomerIdentifier("A"));
        Customer b = new Customer();
        b.setCustomerIdentifier(new CustomerIdentifier("B"));
        repo.saveAll(List.of(a, b));

        repo.delete(a);
        List<Customer> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("B", all.get(0).getCustomerIdentifier().getCustomerId());
    }

    // Testing that embeddable Address and PhoneNumber fields are saved and loaded correctly (positive case)
    @Test
    public void whenSaveWithAddressAndPhones_thenLoadEmbeddables() {
        Customer c = new Customer();
        c.setCustomerIdentifier(new CustomerIdentifier(SAMPLE_ID));
        c.setCustomerAddress(new Address("123","PC","City","Prov"));
        c.setPhoneNumbers(List.of(
                new PhoneNumber(PhoneType.HOME,"111"),
                new PhoneNumber(PhoneType.MOBILE,"222")
        ));
        repo.save(c);

        Customer loaded = repo.findCustomerByCustomerIdentifier_CustomerId(SAMPLE_ID);
        assertEquals("City", loaded.getCustomerAddress().getCity());
        assertEquals(2, loaded.getPhoneNumbers().size());
    }


    // Testing that saving two customers with the same identifier violates the unique constraint (negative case)
    @Test
    public void whenSaveDuplicateIdentifier_thenThrowException() {
        Customer first = new Customer();
        first.setCustomerIdentifier(new CustomerIdentifier(VALID_ID));
        repo.saveAndFlush(first);

        Customer duplicate = new Customer();
        duplicate.setCustomerIdentifier(new CustomerIdentifier(VALID_ID));
        assertThrows(DataIntegrityViolationException.class,
                () -> repo.saveAndFlush(duplicate));
    }

    // Testing that finding by a non-existent email returns null (negative case)
    @Test
    public void whenFindByNonExistingEmail_returnsNull() {
        Customer found = repo.findCustomerByEmailAddress("none@x.com");
        assertNull(found);
    }


    // big test branches

    @Test
    public void combinedValueObjectBranchCoverage() {
        //
        // ─── Address branches ────────────────────────────────────────────────
        //
        Address a1 = new Address("St","P","City","Prov");
        assertEquals(a1, a1);
        assertNotEquals(a1, null);
        assertNotEquals(a1, "foo");
        Address a2 = new Address("St","P","City","Prov");
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertNotEquals(a1, new Address("X","P","City","Prov"));
        assertNotEquals(a1, new Address("St","Q","City","Prov"));
        assertNotEquals(a1, new Address("St","P","Town","Prov"));
        assertNotEquals(a1, new Address("St","P","City","Area"));
        String ats = a1.toString();
        assertTrue(ats.contains("St"));
        assertTrue(ats.contains("P"));
        assertTrue(ats.contains("City"));
        assertTrue(ats.contains("Prov"));

        //
        // ─── PhoneNumber branches ────────────────────────────────────────────
        //
        PhoneNumber p1 = new PhoneNumber(PhoneType.MOBILE, "1234");
        assertEquals(p1, p1);
        assertNotEquals(p1, null);
        assertNotEquals(p1, new Object());
        PhoneNumber p2 = new PhoneNumber(PhoneType.MOBILE, "1234");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, new PhoneNumber(PhoneType.HOME,   "1234"));
        assertNotEquals(p1, new PhoneNumber(PhoneType.MOBILE, "0000"));
        String pts = p1.toString();
        assertTrue(pts.contains("MOBILE"));
        assertTrue(pts.contains("1234"));

        //
        // ─── CustomerIdentifier branches ─────────────────────────────────────
        //
        CustomerIdentifier gen = new CustomerIdentifier();
        assertNotNull(gen.getCustomerId());
        assertDoesNotThrow(() -> UUID.fromString(gen.getCustomerId()));
        assertEquals(gen, gen);
        assertNotEquals(gen, null);
        assertNotEquals(gen, "foo");
        CustomerIdentifier ci1 = new CustomerIdentifier("ABC");
        CustomerIdentifier ci2 = new CustomerIdentifier("ABC");
        CustomerIdentifier ci3 = new CustomerIdentifier("XYZ");
        assertEquals(ci1, ci2);
        assertEquals(ci1.hashCode(), ci2.hashCode());
        assertNotEquals(ci1, ci3);

        //
        // ─── Customer branches ────────────────────────────────────────────────
        //
        Address addr = new Address("A","B","C","D");
        List<PhoneNumber> phones = List.of(
                new PhoneNumber(PhoneType.HOME,   "111"),
                new PhoneNumber(PhoneType.MOBILE, "222")
        );
        CustomerIdentifier id = new CustomerIdentifier("ID001");
        Customer c1 = new Customer(
                1, id, "LN", "FN", "e@mail", "user", "pw", addr, phones
        );
        assertEquals(c1, c1);
        assertNotEquals(c1, null);
        assertNotEquals(c1, "foo");
        Customer c2 = new Customer(
                1,
                new CustomerIdentifier("ID001"),
                "LN","FN","e@mail","user","pw",
                new Address("A","B","C","D"),
                List.of(
                        new PhoneNumber(PhoneType.HOME,   "111"),
                        new PhoneNumber(PhoneType.MOBILE, "222")
                )
        );
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, new Customer(2,   id, "LN","FN","e@mail","user","pw", addr, phones));
        assertNotEquals(c1, new Customer(1, new CustomerIdentifier("DIFF"), "LN","FN","e@mail","user","pw", addr, phones));
        assertNotEquals(c1, new Customer(1,   id, "XX","FN","e@mail","user","pw", addr, phones));
        assertNotEquals(c1, new Customer(1,   id, "LN","XX","e@mail","user","pw", addr, phones));
        assertNotEquals(c1, new Customer(1,   id, "LN","FN","x@mail","user","pw", addr, phones));
        assertNotEquals(c1, new Customer(1,   id, "LN","FN","e@mail","usr2","pw", addr, phones));
        assertNotEquals(c1, new Customer(1,   id, "LN","FN","e@mail","user","pw2", addr, phones));
        assertNotEquals(c1, new Customer(1,   id, "LN","FN","e@mail","user","pw", new Address("X","B","C","D"), phones));
        assertNotEquals(c1, new Customer(1,   id, "LN","FN","e@mail","user","pw", addr, List.of()));
        assertNotEquals(c1, new Customer(1,   id, "LN","FN","e@mail","user","pw", addr, List.of(new PhoneNumber(PhoneType.HOME,"111"))));
        String cts = c1.toString();
        assertTrue(cts.contains("FN"));
        assertTrue(cts.contains("e@mail"));
    }




    }

