package com.sgruendel.nextjs_dashboard.service;

import java.util.List;
import java.util.Locale;

import com.sgruendel.nextjs_dashboard.domain.CustomerWithTotals;
import com.sgruendel.nextjs_dashboard.model.CustomerWithTotalsDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.model.CustomerDTO;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.util.NotFoundException;
import com.sgruendel.nextjs_dashboard.util.WebUtils;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    public CustomerService(final CustomerRepository customerRepository,
            final InvoiceRepository invoiceRepository) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<CustomerDTO> findAll() {
        final List<Customer> customers = customerRepository.findAll(Sort.by("id"));
        return customers.stream()
                .map(customer -> mapToDTO(customer, new CustomerDTO()))
                .toList();
    }

    public CustomerDTO get(final String id) {
        return customerRepository.findById(id)
                .map(customer -> mapToDTO(customer, new CustomerDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public String create(final CustomerDTO customerDTO) {
        final Customer customer = new Customer();
        mapToEntity(customerDTO, customer);
        customer.setId(customerDTO.getId());
        return customerRepository.save(customer).getId();
    }

    public void update(final String id, final CustomerDTO customerDTO) {
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(customerDTO, customer);
        customerRepository.save(customer);
    }

    public void delete(final String id) {
        customerRepository.deleteById(id);
    }

    public CustomerDTO mapToDTO(final Customer customer, final CustomerDTO customerDTO) {
        customerDTO.setId(customer.getId());
        customerDTO.setName(customer.getName());
        customerDTO.setEmail(customer.getEmail());
        customerDTO.setImageUrl(customer.getImageUrl());
        return customerDTO;
    }

    public boolean idExists(final String id) {
        return customerRepository.existsByIdIgnoreCase(id);
    }

    public boolean emailExists(final String email) {
        return customerRepository.existsByEmailIgnoreCase(email);
    }

    public String getReferencedWarning(final String id) {
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        final Invoice customerInvoice = invoiceRepository.findFirstByCustomer(customer);
        if (customerInvoice != null) {
            return WebUtils.getMessage("customer.invoice.customer.referenced", customerInvoice.getId());
        }
        return null;
    }

    public long count() {
        return customerRepository.count();
    }

    public List<CustomerDTO> findAllByOrderByNameAsc(final Pageable pageable) {
        return mapToDTOs(customerRepository.findAllByOrderByNameAsc(pageable));
    }

    public List<CustomerWithTotalsDTO> findAllMatchingSearch(final String query, final Locale locale) {
        return mapToWithTotalsDTOs(customerRepository.findAllMatchingSearch(query, locale));
    }

    private CustomerWithTotalsDTO mapToWithTotalsDTO(final CustomerWithTotals customer,
            final CustomerWithTotalsDTO customerDTO) {
        mapToDTO(customer, customerDTO);
        customerDTO.setTotalInvoices(customer.getTotalInvoices());
        customerDTO.setTotalPending(customer.getTotalPending());
        customerDTO.setTotalPaid(customer.getTotalPaid());
        return customerDTO;
    }

    private List<CustomerDTO> mapToDTOs(final List<Customer> customers) {
        return customers.stream()
                .map(customer -> mapToDTO(customer, new CustomerDTO()))
                .toList();
    }

    private List<CustomerWithTotalsDTO> mapToWithTotalsDTOs(final List<CustomerWithTotals> customers) {
        return customers.stream()
                .map(customer -> mapToWithTotalsDTO(customer, new CustomerWithTotalsDTO()))
                .toList();
    }

    private Customer mapToEntity(final CustomerDTO customerDTO, final Customer customer) {
        customer.setName(customerDTO.getName());
        customer.setEmail(customerDTO.getEmail());
        customer.setImageUrl(customerDTO.getImageUrl());
        return customer;
    }

}
