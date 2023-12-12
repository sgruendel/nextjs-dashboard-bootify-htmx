package com.sgruendel.nextjs_dashboard.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.sgruendel.nextjs_dashboard.model.CustomerDTO;
import com.sgruendel.nextjs_dashboard.model.InvoiceRefDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.model.InvoiceDTO;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.util.NotFoundException;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;

    public InvoiceService(final InvoiceRepository invoiceRepository, final CustomerRepository customerRepository,
            final CustomerService customerService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.customerService = customerService;
    }

    public List<InvoiceDTO> findAll() {
        return mapToDTOs(invoiceRepository.findAll(Sort.by("id")));
    }

    public InvoiceDTO get(final String id) {
        return invoiceRepository.findById(id)
                .map(invoice -> mapToDTO(invoice, new InvoiceDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public String create(final InvoiceRefDTO invoiceRefDTO) {
        final Invoice invoice = new Invoice();
        mapToEntity(invoiceRefDTO, invoice);
        invoice.setId(invoiceRefDTO.getId());
        return invoiceRepository.save(invoice).getId();
    }

    public void update(final String id, final InvoiceRefDTO invoiceRefDTO) {
        final Invoice invoice = invoiceRepository.findById(id).orElseThrow(NotFoundException::new);
        mapToEntity(invoiceRefDTO, invoice);
        invoiceRepository.save(invoice);
    }

    public void delete(final String id) {
        invoiceRepository.deleteById(id);
    }

    public boolean idExists(final String id) {
        return invoiceRepository.existsByIdIgnoreCase(id);
    }

    public Map<String, Long> sumAmountGroupByStatus() {
        return invoiceRepository.sumAmountGroupByStatus();
    }

    public long count() {
        return invoiceRepository.count();
    }

    public List<InvoiceDTO> findFirst5ByOrderByDateDesc() {
        return mapToDTOs(invoiceRepository.findFirst5ByOrderByDateDesc());
    }

    public long countMatchingSearch(final String query, final Locale locale) {
        return invoiceRepository.countMatchingSearch(query, locale);
    }

    public List<InvoiceDTO> findAllMatchingSearch(final String query, final Locale locale, final int pageSize,
            final long pageNumber) {

        return mapToDTOs(invoiceRepository.findAllMatchingSearch(query, locale, pageSize, pageNumber));
    }

    public List<InvoiceDTO> findAllByOrderByDateDesc(final Pageable pageable) {
        return mapToDTOs(invoiceRepository.findAllByOrderByDateDesc(pageable));
    }

    private InvoiceDTO mapToDTO(final Invoice invoice, final InvoiceDTO invoiceDTO) {
        invoiceDTO.setId(invoice.getId());
        invoiceDTO.setAmount(invoice.getAmount());
        invoiceDTO.setStatus(invoice.getStatus());
        invoiceDTO.setDate(invoice.getDate());
        invoiceDTO.setCustomer(new CustomerDTO());
        invoiceDTO.setCustomer(invoice.getCustomer() == null ? null
                : customerService.mapToDTO(invoice.getCustomer(), invoiceDTO.getCustomer()));
        return invoiceDTO;
    }

    private List<InvoiceDTO> mapToDTOs(final List<Invoice> invoices) {
        return invoices.stream()
                .map(invoice -> mapToDTO(invoice, new InvoiceDTO()))
                .toList();
    }

    private Invoice mapToEntity(final InvoiceRefDTO invoiceRefDTO, final Invoice invoice) {
        invoice.setAmount(invoiceRefDTO.getAmount());
        invoice.setStatus(invoiceRefDTO.getStatus());
        invoice.setDate(invoiceRefDTO.getDate());
        final Customer customer = invoiceRefDTO.getCustomer() == null ? null
                : customerRepository.findById(invoiceRefDTO.getCustomer())
                        .orElseThrow(() -> new NotFoundException("customer not found"));
        invoice.setCustomer(customer);
        return invoice;
    }

}
