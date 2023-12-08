package com.sgruendel.nextjs_dashboard.controller;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.CustomerWithTotals;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.model.InvoiceDTO;
import com.sgruendel.nextjs_dashboard.model.Status;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;
import com.sgruendel.nextjs_dashboard.service.InvoiceService;
import com.sgruendel.nextjs_dashboard.ui.BreadcrumbData;
import com.sgruendel.nextjs_dashboard.ui.CardData;
import com.sgruendel.nextjs_dashboard.ui.LinkData;
import com.sgruendel.nextjs_dashboard.ui.PaginationData;
import com.sgruendel.nextjs_dashboard.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private static final List<LinkData> LINKS = List.of(
            new LinkData("Home", "/dashboard", "home-icon", false),
            new LinkData("Invoices", "/dashboard/invoices", "document-duplicate-icon", false),
            new LinkData("Customers", "/dashboard/customers", "user-group-icon", false));

    private static final int INVOICES_PER_PAGE = 6;

    private final InvoiceService invoiceService;

    // TODO only use services in controller
    private final CustomerRepository customerRepository;

    private final InvoiceRepository invoiceRepository;

    private final RevenueRepository revenueRepository;

    public DashboardController(InvoiceService invoiceService, CustomerRepository customerRepository,
            InvoiceRepository invoiceRepository, RevenueRepository revenueRepository) {

        this.invoiceService = invoiceService;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.revenueRepository = revenueRepository;
    }

    @ModelAttribute("links")
    public List<LinkData> getLinks(final HttpServletRequest request) {
        final String requestURI = request.getRequestURI();
        final List<LinkData> links = new LinkedList<>();
        LINKS.forEach(link -> links.add(new LinkData(link.getName(), link.getHref(), link.getIcon(),
                link.getHref().equals(requestURI))));
        return links;
    }

    @GetMapping
    public String dashboard(Model model) {
        // TODO name of method/page
        return "dashboard/page";
    }

    @GetMapping("/cards")
    public String cards(final Model model) throws InterruptedException {
        // Thread.sleep(3000);

        final Map<String, Long> sumOfAmountByStatus = invoiceRepository.sumAmountGroupByStatus();

        final List<CardData> cards = List.of(
                new CardData<String>("banknotes-icon", "Collected",
                        WebUtils.formatCurrency(sumOfAmountByStatus.get(Status.PAID.name()))),
                new CardData<String>("clock-icon", "Pending",
                        WebUtils.formatCurrency(sumOfAmountByStatus.get(Status.PENDING.name()))),
                new CardData<Long>("inbox-icon", "Total Invoices", invoiceRepository.count()),
                new CardData<Long>("user-group-icon", "Total Customers", customerRepository.count()));

        model.addAttribute("cards", cards);
        return "fragments/dashboard/cards :: cards";
    }

    @GetMapping("/revenue-chart")
    public String revenueChart(Model model) throws InterruptedException {
        // Thread.sleep(3000);

        // const revenues = await Revenues.find().lean().select(['month',
        // 'revenue']).exec();
        List<Revenue> revenues = revenueRepository.findAll();
        if (revenues.isEmpty()) {
            return "fragments/dashboard/revenue-chart :: revenue-chart-empty";
        }

        model.addAttribute("revenues", revenues);

        // Calculate what labels we need to display on the y-axis
        // based on highest record and in 1000s
        @SuppressWarnings("OptionalGetWithoutIsPresent") // it's not empty
        final int highestRevenue = revenues.stream().max(Comparator.comparingInt(Revenue::getRevenue)).get()
                .getRevenue();

        final int topLabel = (int) Math.ceil(highestRevenue / 1000.0) * 1000;
        model.addAttribute("topLabel", topLabel);

        final List<String> yAxisLabels = new LinkedList<>();
        for (int i = topLabel; i >= 0; i -= 1000) {
            yAxisLabels.add("$" + (i / 1000) + "K");
        }
        model.addAttribute("yAxisLabels", yAxisLabels);

        return "fragments/dashboard/revenue-chart :: revenue-chart";
    }

    @GetMapping("/latest-invoices")
    public String latestInvoices(Model model) {

        final List<Invoice> latestInvoices = invoiceRepository.findFirst5ByOrderByDateDesc();

        model.addAttribute("latestInvoices", latestInvoices);
        return "fragments/dashboard/latest-invoices :: latest-invoices";
    }

    @GetMapping("/invoices")
    public String invoices() {

        return "dashboard/invoices";
    }

    @GetMapping("/invoices/table")
    public String invoicesTable(@RequestParam(required = false) final String query,
            @RequestParam(required = false, defaultValue = "1") final long page, final Locale locale, final Model model,
            final HttpServletResponse response) {

        LOGGER.info("querying invoices for '{}'", query);

        final List<Invoice> invoices;
        final long totalItems;
        if (StringUtils.hasText(query)) {
            invoices = invoiceRepository.findAllMatchingSearch(query, locale, INVOICES_PER_PAGE, page - 1);
            totalItems = invoiceRepository.countMatchingSearch(query, locale);

            // add query params to URL
            response.addHeader("HX-Replace-Url", "?page=" + page + "&query=" + query);
        } else {
            invoices = invoiceRepository
                    .findAllByOrderByDateDesc(Pageable.ofSize(INVOICES_PER_PAGE).withPage((int) page - 1));
            totalItems = invoiceRepository.count();
        }

        addPaginationAttributes(model, page, totalItems, INVOICES_PER_PAGE);
        model.addAttribute("invoices", invoices);

        return "fragments/invoices/table :: invoices-table";
    }

    @GetMapping("/invoices/create")
    public String invoicesCreate(@ModelAttribute("invoice") final InvoiceDTO invoiceDTO, final Model model) {

        final List<BreadcrumbData> breadcrumbs = List.of(
                new BreadcrumbData("Invoices", "/dashboard/invoices", false),
                new BreadcrumbData("Create Invoice", "/dashboard/invoices/create", true));
        model.addAttribute("breadcrumbs", breadcrumbs);

        final List<Customer> customers = customerRepository.findAllByOrderByNameAsc(Pageable.unpaged());
        model.addAttribute("customers", customers);

        // set value for date as it is marked as @NotNull, so the form can resend it
        invoiceDTO.setDate(LocalDateTime.now());

        return "dashboard/invoice-create";
    }

    @PostMapping("/invoices/create")
    public String createInvoice(@ModelAttribute("invoice") @Valid final InvoiceDTO invoiceDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            LOGGER.info("binding errors {}", bindingResult.getAllErrors());
            bindingResult.getAllErrors().forEach(error -> LOGGER.info("binding error {}", error));
            // TODO model needs all attributes, better redirect to /invoices/create?
            return "dashboard/invoice-create";
        }

        // reset date to be server side now()
        // invoiceDTO.setDate(LocalDateTime.now());

        String id = invoiceService.create(invoiceDTO);
        LOGGER.info("created invoice id {}", id);
        // TODO redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS,
        // WebUtils.getMessage("invoice.create.success"));
        // TODO return "redirect:/invoices";
        return "dashboard/invoices";
    }

    @GetMapping("/invoices/edit/{id}")
    public String editInvoice(@PathVariable final String id, final Model model) {
        final List<BreadcrumbData> breadcrumbs = List.of(
                new BreadcrumbData("Invoices", "/dashboard/invoices", false),
                new BreadcrumbData("Edit Invoice", "/dashboard/invoices/edit/" + id, true));
        model.addAttribute("breadcrumbs", breadcrumbs);

        final List<Customer> customers = customerRepository.findAllByOrderByNameAsc(Pageable.unpaged());
        model.addAttribute("customers", customers);
        model.addAttribute("invoice", invoiceService.get(id));
        return "dashboard/invoice-edit";
    }

    @PostMapping("/invoices/edit/{id}")
    public String editInvoice(@PathVariable final String id,
            @ModelAttribute("invoice") @Valid final InvoiceDTO invoiceDTO,
            final BindingResult bindingResult, final RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            LOGGER.info("binding errors {}", bindingResult.getAllErrors());
            bindingResult.getAllErrors().forEach(error -> LOGGER.info("binding error {}", error));
            // TODO model needs all attributes, better redirect to /invoices/edit?
            return "dashboard/invoice-edit";
        }
        invoiceService.update(id, invoiceDTO);
        // TODO redirectAttributes.addFlashAttribute(WebUtils.MSG_SUCCESS,
        // WebUtils.getMessage("invoice.update.success"));
        // TODO return "redirect:/invoices";
        return "dashboard/invoices";
    }

    @DeleteMapping("/invoices/delete/{id}")
    public String deleteInvoice(@PathVariable final String id, final RedirectAttributes redirectAttributes) {
        invoiceService.delete(id);
        // TODO redirectAttributes.addFlashAttribute(WebUtils.MSG_INFO,
        // WebUtils.getMessage("invoice.delete.success"));
        // TODO return "redirect:/dashboard/invoices";
        return "/dashboard/invoices";
    }

    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) final String query,
            @RequestParam(required = false, defaultValue = "1") final Locale locale, final Model model) {

        final List<CustomerWithTotals> customers = customerRepository.findAllMatchingSearch(query == null ? "" : query,
                locale);
        model.addAttribute("customers", customers);
        return "dashboard/customers";
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
