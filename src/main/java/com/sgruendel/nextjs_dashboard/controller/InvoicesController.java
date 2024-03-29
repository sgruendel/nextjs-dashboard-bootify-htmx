package com.sgruendel.nextjs_dashboard.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sgruendel.nextjs_dashboard.model.CustomerDTO;
import com.sgruendel.nextjs_dashboard.model.InvoiceDTO;
import com.sgruendel.nextjs_dashboard.model.InvoiceFormDTO;
import com.sgruendel.nextjs_dashboard.service.CustomerService;
import com.sgruendel.nextjs_dashboard.service.InvoiceService;
import com.sgruendel.nextjs_dashboard.ui.BreadcrumbData;
import com.sgruendel.nextjs_dashboard.ui.PaginationData;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
@PreAuthorize("hasAuthority('ROLE_USER')")
@RequestMapping("/dashboard/invoices")
public class InvoicesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoicesController.class);

    private static final int INVOICES_PER_PAGE = 6;

    private final CustomerService customerService;

    private final InvoiceService invoiceService;

    public InvoicesController(CustomerService customerService, InvoiceService invoiceService) {
        this.customerService = customerService;
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public String invoices(@RequestParam(required = false, defaultValue = "1") final long page, final Model model) {

        // needed for pagination, query is handled via URL param
        model.addAttribute("page", page);
        return "dashboard/invoices";
    }

    @GetMapping("/table")
    public String invoicesTable(@RequestParam(required = false) final String query,
            @RequestParam(required = false, defaultValue = "1") final long page, final Locale locale, final Model model,
            final HttpServletResponse response) {

        LOGGER.info("querying invoices for '{}'", query);

        final List<InvoiceDTO> invoices;
        final long totalItems;
        if (StringUtils.hasText(query)) {
            invoices = invoiceService.findAllMatchingSearch(query, locale, INVOICES_PER_PAGE, page - 1);
            totalItems = invoiceService.countMatchingSearch(query, locale);

            // add query params to URL
            response.addHeader("HX-Replace-Url", "?page=" + page + "&query=" + query);
        } else {
            invoices = invoiceService
                    .findAllByOrderByDateDesc(Pageable.ofSize(INVOICES_PER_PAGE).withPage((int) page - 1));
            totalItems = invoiceService.count();

            // add query params to URL, leave out query param so it gets deleted from URL
            response.addHeader("HX-Replace-Url", "?page=" + page);
        }

        addPaginationAttributes(model, page, totalItems, INVOICES_PER_PAGE);
        model.addAttribute("invoices", invoices);

        return "fragments/invoices/table :: invoices-table";
    }

    @GetMapping("/create")
    public String invoicesCreate(@ModelAttribute("invoice") final InvoiceFormDTO invoiceFormDTO, final Model model) {

        addInvoiceFormAttributes(model, null);
        return "dashboard/invoice-create";
    }

    @PostMapping("/create")
    public String createInvoice(@ModelAttribute("invoice") @Valid final InvoiceFormDTO invoiceFormDTO,
            final BindingResult bindingResult, final Model model, final RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> LOGGER.info("binding error {}", error));
            addInvoiceFormAttributes(model, null);
            return "dashboard/invoice-create";
        }

        String id = invoiceService.create(invoiceFormDTO);
        LOGGER.info("created invoice id {}", id);
        // TODO redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS,
        // WebUtils.getMessage("invoice.create.success"));
        // TODO return "redirect:/invoices";
        return "dashboard/invoices";
    }

    @GetMapping("/edit/{id}")
    public String editInvoice(@PathVariable final String id, final Model model) {
        addInvoiceFormAttributes(model, id);
        model.addAttribute("invoice", invoiceService.get(id));
        return "dashboard/invoice-edit";
    }

    @PostMapping("/edit/{id}")
    public String editInvoice(@PathVariable final String id,
            @ModelAttribute("invoice") @Valid final InvoiceFormDTO invoiceFormDTO,
            final BindingResult bindingResult, final Model model, final RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> LOGGER.info("binding error {}", error));
            addInvoiceFormAttributes(model, id);
            return "dashboard/invoice-edit";
        }
        invoiceService.update(id, invoiceFormDTO);
        // TODO redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS,
        // WebUtils.getMessage("invoice.update.success"));
        // TODO return "redirect:/invoices";
        return "dashboard/invoices";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable final String id, final RedirectAttributes redirectAttributes) {
        invoiceService.delete(id);
        // TODO redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO,
        // WebUtils.getMessage("invoice.delete.success"));
        // TODO return "redirect:/dashboard/invoices";
        return "/dashboard/invoices";
    }

    /**
     * Adds pagination attributes to a model, including page number, total number
     * of pages, start and end indexes, total number of items, and a list of
     * pagination links.
     *
     * @param model        The model to use in the view.
     * @param page         The current page number.
     * @param totalItems   The total number of items in the collection or dataset
     *                     that you want to paginate.
     * @param itemsPerPage The number of items to be displayed per page.
     */
    private void addPaginationAttributes(final Model model, final long page, final long totalItems,
            final int itemsPerPage) {

        final long startIndex = (page - 1) * itemsPerPage + 1;
        final long endIndex = Math.min(page * itemsPerPage, totalItems);
        final long totalPages = Double.valueOf(Math.ceil((double) totalItems / itemsPerPage)).longValue();
        LOGGER.info("pagination attributes from page={}, total items={}: start index={}, end index={}, total pages={}",
                page, totalItems, startIndex, endIndex, totalPages);

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("paginations", createPaginations(page, totalPages));
    }

    /**
     * Add attributes to a model for create/edit invoice form, including breadcrumbs
     * and a list of customers.
     *
     * @param model The model to use in the view.
     * @param id    ID of an invoice. If {@code null}, a new invoice is being
     *              created.
     */
    private void addInvoiceFormAttributes(final Model model, final String id) {
        final List<BreadcrumbData> breadcrumbs = List.of(
                new BreadcrumbData("Invoices", "/dashboard/invoices", false),
                new BreadcrumbData(
                        id == null ? "Create Invoice" : "Edit Invoice",
                        id == null ? "/dashboard/invoices/create" : ("/dashboard/invoices/edit/" + id),
                        true));
        model.addAttribute("breadcrumbs", breadcrumbs);

        final List<CustomerDTO> customers = customerService.findAllByOrderByNameAsc(Pageable.unpaged());
        model.addAttribute("customers", customers);
    }

    /**
     * Creates a list of pagination data based on the current page and total number
     * of pages.
     *
     * @param page       The current page number.
     * @param totalPages The total number of pages in the pagination.
     * @return list of `PaginationData` objects.
     */
    private List<PaginationData> createPaginations(final long page, final long totalPages) {
        final List<String> texts;

        // If the total number of pages is 7 or less,
        // display all pages without any ellipsis.
        if (totalPages <= 7) {
            texts = LongStream.rangeClosed(1, totalPages).boxed().map(Object::toString).collect(Collectors.toList());
        } else

        // If the current page is among the first 3 pages,
        // show the first 3, an ellipsis, and the last 2 pages.
        if (page <= 3) {
            texts = List.of("1", "2", "3", "...", String.valueOf(totalPages - 1), String.valueOf(totalPages));
        } else

        // If the current page is among the last 3 pages,
        // show the first 2, an ellipsis, and the last 3 pages.
        if (page >= totalPages - 2) {
            texts = List.of("1", "2", "...", String.valueOf(totalPages - 2), String.valueOf(totalPages - 1),
                    String.valueOf(totalPages));
        } else

        // If the current page is somewhere in the middle,
        // show the first page, an ellipsis, the current page and its neighbors,
        // another ellipsis, and the last page.
        {
            texts = List.of("1", "...", String.valueOf(page - 1), String.valueOf(page),
                    String.valueOf(page + 1), "...", String.valueOf(totalPages));
        }

        final List<PaginationData> paginations = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i++) {
            final String text = texts.get(i);
            final String position;
            if (i == 0) {
                position = "first";
            } else if (i == texts.size() - 1) {
                position = "last";
            } else if (texts.size() == 1) {
                position = "single";
            } else if (text.equals("...")) {
                position = "middle";
            } else {
                position = "";
            }
            paginations.add(new PaginationData(text, position));
        }
        return paginations;
    }

}
