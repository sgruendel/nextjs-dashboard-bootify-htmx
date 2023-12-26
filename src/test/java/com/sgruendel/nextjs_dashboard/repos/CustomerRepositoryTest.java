package com.sgruendel.nextjs_dashboard.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.model.Status;

@DataMongoTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    private static final String FIRST_NAME = "Firstname";

    private static final String LAST_NAME = "Lastname";

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private List<Customer> customers;
    private List<Invoice> invoices;

    @BeforeEach
    public void setupTestData() {
        final Customer customer1 = new Customer();
        customer1.setName(FIRST_NAME + " " + LAST_NAME);
        customer1.setEmail("User@Email.Org");
        customer1.setImageUrl("/image URL");
        // customer1.setInvoices(Set.of(invoicePaid, invoicePending));

        final Invoice invoicePaid = new Invoice();
        invoicePaid.setAmount(1234);
        invoicePaid.setDate(LocalDateTime.now());
        invoicePaid.setStatus(Status.PAID);
        invoicePaid.setCustomer(customer1);

        final Invoice invoicePending = new Invoice();
        invoicePending.setAmount(4567);
        invoicePending.setDate(LocalDateTime.now());
        invoicePending.setStatus(Status.PENDING);
        invoicePending.setCustomer(customer1);

        customers = List.of(customer1);
        invoices = List.of(invoicePaid, invoicePending);
    }

    @Test
    void testEmptyRepository() {
        assertEquals(0, customerRepository.count());
        assertEquals(0, customerRepository.findAll().size());
    }

    @Test
    void testInitializedRepository() {
        customers.forEach(customerRepository::save);
        assertEquals(customers.size(), customerRepository.count());
        assertEquals(customers.size(), customerRepository.findAll().size());
    }

    @Test
    void testExistsByEmailIgnoreCase() {
        customers.forEach(customerRepository::save);

        assertFalse(customerRepository.existsByEmailIgnoreCase("unknown email"));
        assertTrue(customerRepository.existsByEmailIgnoreCase(customers.get(0).getEmail()));
        assertTrue(customerRepository.existsByEmailIgnoreCase(customers.get(0).getEmail().toLowerCase()));
        assertTrue(customerRepository.existsByEmailIgnoreCase(customers.get(0).getEmail().toUpperCase()));
    }

    @Test
    void testExistsById() {
        customers.forEach(customerRepository::save);

        assertFalse(customerRepository.existsById("unknown id"));
        assertTrue(customerRepository.existsById(customers.get(0).getId()));
    }

    @Test
    void testFindAllByOrderByNameAsc() {
        customers.forEach(customerRepository::save);

        assertEquals(1, customerRepository.findAllByOrderByNameAsc(Pageable.unpaged()).size());
    }

    @Test
    void testFindByEmailIgnoreCase() {
        customers.forEach(customerRepository::save);

        assertNull(customerRepository.findByEmailIgnoreCase("unknown email"));
        assertEquals(customers.get(0).getId(), customerRepository.findByEmailIgnoreCase(customers.get(0).getEmail()).getId());
        assertEquals(customers.get(0).getId(), customerRepository.findByEmailIgnoreCase(customers.get(0).getEmail().toLowerCase()).getId());
        assertEquals(customers.get(0).getId(), customerRepository.findByEmailIgnoreCase(customers.get(0).getEmail().toUpperCase()).getId());
    }

    @Test
    void findAllMatchingSearch() {
        customers.forEach(customerRepository::save);
        invoices.forEach(invoiceRepository::save);
        final Customer customer = customers.get(0);

        assertEquals(0, customerRepository.findAllMatchingSearch("search matching nothing", Locale.US).size());

        assertEquals(1, customerRepository.findAllMatchingSearch(FIRST_NAME, Locale.US).size());
        assertEquals(1, customerRepository.findAllMatchingSearch(FIRST_NAME.toLowerCase(), Locale.US).size());
        assertEquals(1, customerRepository.findAllMatchingSearch(FIRST_NAME.toUpperCase(), Locale.US).size());

        assertEquals(1, customerRepository.findAllMatchingSearch(LAST_NAME, Locale.US).size());
        assertEquals(1, customerRepository.findAllMatchingSearch(LAST_NAME.toLowerCase(), Locale.US).size());
        assertEquals(1, customerRepository.findAllMatchingSearch(LAST_NAME.toUpperCase(), Locale.US).size());

        assertEquals(1, customerRepository.findAllMatchingSearch(customer.getEmail(), Locale.US).size());
        assertEquals(1, customerRepository.findAllMatchingSearch(customer.getEmail().toLowerCase(), Locale.US).size());
        assertEquals(1, customerRepository.findAllMatchingSearch(customer.getEmail().toUpperCase(), Locale.US).size());

        assertEquals(0, customerRepository.findAllMatchingSearch(customer.getImageUrl(), Locale.US).size());
        assertEquals(0,
                customerRepository.findAllMatchingSearch(customer.getImageUrl().toLowerCase(), Locale.US).size());
        assertEquals(0,
                customerRepository.findAllMatchingSearch(customer.getImageUrl().toUpperCase(), Locale.US).size());
    }

    @AfterEach
    void tearDown() {
        // no transactions with local MongoDB so we must clean up after each test
        customerRepository.deleteAll();
        invoiceRepository.deleteAll();
    }

}
