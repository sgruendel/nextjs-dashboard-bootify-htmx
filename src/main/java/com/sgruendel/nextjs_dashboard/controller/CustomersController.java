package com.sgruendel.nextjs_dashboard.controller;

import com.sgruendel.nextjs_dashboard.model.CustomerWithTotalsDTO;
import com.sgruendel.nextjs_dashboard.service.CustomerService;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

@Controller
@PreAuthorize("hasAuthority('ROLE_USER')")
@RequestMapping("/dashboard/customers")
public class CustomersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomersController.class);

    private final CustomerService customerService;

    public CustomersController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String customers(@RequestParam(required = false) final String query,
            @RequestParam(required = false, defaultValue = "1") final Locale locale, final Model model) {

        final List<CustomerWithTotalsDTO> customers = customerService.findAllMatchingSearch(query == null ? "" : query,
                locale);
        model.addAttribute("customers", customers);
        return "dashboard/customers";
    }

    @GetMapping("/table")
    public String customersTable(@RequestParam(required = false) final String query, final Locale locale,
            final Model model, final HttpServletResponse response) {

        LOGGER.info("querying customers for '{}'", query);

        final List<CustomerWithTotalsDTO> customers = customerService.findAllMatchingSearch(query == null ? "" : query,
                locale);
        model.addAttribute("customers", customers);

        // add query params to URL
        response.addHeader("HX-Replace-Url", "?query=" + query);

        return "fragments/customers/table :: customers-table";
    }

}
