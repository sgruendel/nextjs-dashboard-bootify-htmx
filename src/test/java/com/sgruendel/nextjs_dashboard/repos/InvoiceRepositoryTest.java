package com.sgruendel.nextjs_dashboard.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
class InvoiceRepositoryTest {

        private static final String FIRST_NAME = "Firstname";
        private static final String LAST_NAME = "Lastname";
        private static final int PENDING_AMOUNT = 1234;
        private static final int PAID_AMOUNT = 4567;

        @Autowired
        private InvoiceRepository invoiceRepository;

        @Autowired
        private CustomerRepository customerRepository;

        private List<Invoice> invoices;
        private List<Customer> customers;

        @BeforeEach
        public void setupTestData() {
                final Customer customer1 = new Customer();
                customer1.setName(FIRST_NAME + " " + LAST_NAME);
                customer1.setEmail("User@Email.Org");
                customer1.setImageUrl("/image URL");

                final Invoice invoicePending = new Invoice();
                invoicePending.setAmount(PENDING_AMOUNT);
                invoicePending.setDate(LocalDateTime.now());
                invoicePending.setStatus(Status.PENDING);
                invoicePending.setCustomer(customer1);

                final Invoice invoicePaid = new Invoice();
                invoicePaid.setAmount(PAID_AMOUNT);
                invoicePaid.setDate(LocalDateTime.now());
                invoicePaid.setStatus(Status.PAID);
                invoicePaid.setCustomer(customer1);

                customer1.setInvoices(Set.of(invoicePaid, invoicePending));
                customers = List.of(customer1);
                invoices = List.of(invoicePending, invoicePaid);
        }

        @Test
        void testEmptyRepository() {
                assertEquals(0, invoiceRepository.count());
                assertEquals(0, invoiceRepository.findAll().size());
        }

        @Test
        void testInitializedRepository() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                assertEquals(invoices.size(), invoiceRepository.count());
                assertEquals(invoices.size(), invoiceRepository.findAll().size());
        }

        @Test
        void testExistsById() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                assertFalse(invoiceRepository.existsById("unknown id"));
                invoices.forEach(invoice -> assertTrue(invoiceRepository.existsById(invoice.getId())));
        }

        @Test
        void testFindAllByOrderByDateDesc() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                assertEquals(invoices.size(), invoiceRepository.findAllByOrderByDateDesc(Pageable.unpaged()).size());
        }

        @Test
        void testFindFirst5ByOrderByDateDesc() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                assertEquals(invoices.size(), invoiceRepository.findFirst5ByOrderByDateDesc().size());

                for (int i = 0; i <= 5; i++) {
                        Invoice invoice = new Invoice();
                        invoice.setAmount(i * 1000);
                        invoice.setDate(LocalDateTime.now());
                        invoice.setStatus(Status.PENDING);
                        invoice.setCustomer(customers.get(0));
                        invoiceRepository.save(invoice);
                }
                assertEquals(5, invoiceRepository.findFirst5ByOrderByDateDesc().size());
        }

        @Test
        void testFindFirstByCustomer() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                final Invoice invoice = invoiceRepository.findFirstByCustomer(customers.get(0));
                assertNotNull(invoice);
        }

        @Test
        void testSumAmountGroupByStatus() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                final Map<String, Long> sums = invoiceRepository.sumAmountGroupByStatus();
                assertEquals(2, sums.size());
                assertEquals(PENDING_AMOUNT, sums.get(Status.PENDING.name()));
                assertEquals(PAID_AMOUNT, sums.get(Status.PAID.name()));
        }

        @Test
        void testFindAllMatchingSearch() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);
                final int PAGE_SIZE = 5;
                final int PAGE_NUMBER = 0;

                assertEquals(0, invoiceRepository
                                .findAllMatchingSearch("search matching nothing", Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());

                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch(FIRST_NAME, Locale.US, PAGE_SIZE, PAGE_NUMBER).size());
                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch(FIRST_NAME.toLowerCase(), Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());
                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch(FIRST_NAME.toUpperCase(), Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());

                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch(LAST_NAME, Locale.US, PAGE_SIZE, PAGE_NUMBER).size());
                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch(LAST_NAME.toLowerCase(), Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());
                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch(LAST_NAME.toUpperCase(), Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());

                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch("Email", Locale.US, PAGE_SIZE, PAGE_NUMBER).size());
                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch("email", Locale.US, PAGE_SIZE, PAGE_NUMBER).size());
                assertEquals(2, invoiceRepository
                                .findAllMatchingSearch("EMAIL", Locale.US, PAGE_SIZE, PAGE_NUMBER).size());

                assertEquals(1, invoiceRepository
                                .findAllMatchingSearch(String.valueOf(PENDING_AMOUNT), Locale.US, PAGE_SIZE,
                                                PAGE_NUMBER)
                                .size());
                assertEquals(1, invoiceRepository
                                .findAllMatchingSearch(String.valueOf(PAID_AMOUNT), Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());

                final String dayOfMonth = String.valueOf(LocalDateTime.now().getDayOfMonth());
                assertEquals(2,
                                invoiceRepository.findAllMatchingSearch(dayOfMonth, Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                                .size(),
                                "invoices found for day " + dayOfMonth);
                final String month = String
                                .valueOf(LocalDateTime.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.US));
                assertEquals(2,
                                invoiceRepository.findAllMatchingSearch(month, Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                                .size(),
                                "invoices found for month " + month);
                assertEquals(2,
                                invoiceRepository.findAllMatchingSearch(month.toLowerCase(), Locale.US, PAGE_SIZE,
                                                PAGE_NUMBER).size(),
                                "invoices found for month " + month.toLowerCase());
                assertEquals(2,
                                invoiceRepository.findAllMatchingSearch(month.toUpperCase(), Locale.US, PAGE_SIZE,
                                                PAGE_NUMBER).size(),
                                "invoices found for month " + month.toUpperCase());
                final String year = String.valueOf(LocalDateTime.now().getYear());
                assertEquals(2,
                                invoiceRepository.findAllMatchingSearch(year, Locale.US, PAGE_SIZE, PAGE_NUMBER).size(),
                                "invoices found for year " + year);

                assertEquals(1, invoiceRepository
                                .findAllMatchingSearch(Status.PENDING.name().toLowerCase(), Locale.US, PAGE_SIZE,
                                                PAGE_NUMBER)
                                .size());
                assertEquals(1, invoiceRepository
                                .findAllMatchingSearch(Status.PENDING.name(), Locale.US, PAGE_SIZE, PAGE_NUMBER)
                                .size());
                assertEquals(1, invoiceRepository
                                .findAllMatchingSearch(Status.PAID.name().toLowerCase(), Locale.US, PAGE_SIZE,
                                                PAGE_NUMBER)
                                .size());
                assertEquals(1, invoiceRepository
                                .findAllMatchingSearch(Status.PAID.name(), Locale.US, PAGE_SIZE, PAGE_NUMBER).size());
        }

        @Test
        void testCountMatchingSearch() {
                customers.forEach(customerRepository::save);
                invoices.forEach(invoiceRepository::save);

                assertEquals(0, invoiceRepository.countMatchingSearch("search matching nothing", Locale.US));

                assertEquals(2, invoiceRepository.countMatchingSearch(FIRST_NAME, Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch(FIRST_NAME.toLowerCase(), Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch(FIRST_NAME.toUpperCase(), Locale.US));

                assertEquals(2, invoiceRepository.countMatchingSearch(LAST_NAME, Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch(LAST_NAME.toLowerCase(), Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch(LAST_NAME.toUpperCase(), Locale.US));

                assertEquals(2, invoiceRepository.countMatchingSearch("Email", Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch("email", Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch("EMAIL", Locale.US));

                assertEquals(1, invoiceRepository.countMatchingSearch(String.valueOf(PENDING_AMOUNT), Locale.US));
                assertEquals(1, invoiceRepository.countMatchingSearch(String.valueOf(PAID_AMOUNT), Locale.US));

                final String dayOfMonth = String.valueOf(LocalDateTime.now().getDayOfMonth());
                assertEquals(2, invoiceRepository.countMatchingSearch(dayOfMonth, Locale.US),
                                "invoices found for day " + dayOfMonth);
                final String month = String
                                .valueOf(LocalDateTime.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.US));
                assertEquals(2, invoiceRepository.countMatchingSearch(month, Locale.US),
                                "invoices found for month " + month);
                assertEquals(2, invoiceRepository.countMatchingSearch(month.toLowerCase(), Locale.US),
                                "invoices found for month " + month.toLowerCase());
                assertEquals(2, invoiceRepository.countMatchingSearch(month.toUpperCase(), Locale.US),
                                "invoices found for month " + month.toUpperCase());
                final String year = String.valueOf(LocalDateTime.now().getYear());
                assertEquals(2, invoiceRepository.countMatchingSearch(year, Locale.US),
                                "invoices found for year " + year);

                assertEquals(1, invoiceRepository.countMatchingSearch(Status.PENDING.name().toLowerCase(), Locale.US));
                assertEquals(1, invoiceRepository.countMatchingSearch(Status.PENDING.name(), Locale.US));
                assertEquals(1, invoiceRepository.countMatchingSearch(Status.PAID.name().toLowerCase(), Locale.US));
                assertEquals(1, invoiceRepository.countMatchingSearch(Status.PAID.name(), Locale.US));
        }

        @AfterEach
        void tearDown() {
                // no transactions with local MongoDB so we must clean up after each test
                invoiceRepository.deleteAll();
                customerRepository.deleteAll();
        }

}
