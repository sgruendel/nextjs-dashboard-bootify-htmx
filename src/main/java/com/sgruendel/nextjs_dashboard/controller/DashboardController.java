package com.sgruendel.nextjs_dashboard.controller;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;
import com.sgruendel.nextjs_dashboard.ui.BreadcrumbData;
import com.sgruendel.nextjs_dashboard.ui.LinkData;
import com.sgruendel.nextjs_dashboard.ui.PaginationData;
import com.sgruendel.nextjs_dashboard.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Controller
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private static final List<LinkData> LINKS = List.of(
            new LinkData("Home", "/dashboard", "home-icon", false),
            new LinkData("Invoices", "/dashboard/invoices", "document-duplicate-icon", false),
            new LinkData("Customers", "/dashboard/customers", "user-group-icon", false));

    private static final int INVOICES_PER_PAGE = 6;

    private final CustomerRepository customerRepository;

    private final InvoiceRepository invoiceRepository;

    private final RevenueRepository revenueRepository;

    public DashboardController(CustomerRepository customerRepository, InvoiceRepository invoiceRepository,
            RevenueRepository revenueRepository) {

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

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // TODO name of method/page
        return "dashboard/page";
    }

    @GetMapping("/dashboard/card")
    public String card(@RequestParam String icon, @RequestParam String title, @RequestParam String type)
            throws InterruptedException {
        /*
         * optimized version, not supported in VScode yet:
         * final long value = switch (type) {
         * case "collected" -> 164116;
         * case "pending" -> 137932;
         * case "customers" -> {
         * yield customerRepository.count();
         * }
         * case "invoices" -> {
         * yield invoiceRepository.count();
         * }
         * default -> throw new IllegalArgumentException("Unknown type: " + type);
         * };
         */
        final String value;
        switch (type) {
            case "collected":
                value = WebUtils.formatCurrency(invoiceRepository.sumAmountGroupByStatus().get("paid"));
                break;

            case "pending":
                value = WebUtils.formatCurrency(invoiceRepository.sumAmountGroupByStatus().get("pending"));
                break;

            case "customers":
                Thread.sleep(3000);
                value = String.valueOf(customerRepository.count());
                break;

            case "invoices":
                Thread.sleep(1000);
                value = String.valueOf(invoiceRepository.count());
                break;

            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
        return "fragments/dashboard/cards :: card(icon='" + icon + "', title='" + title + "', value='" + value + "')";
    }

    @GetMapping("/dashboard/revenue-chart")
    public String revenueChart(Model model) throws InterruptedException {
        Thread.sleep(3000);

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

    @GetMapping("/dashboard/latest-invoices")
    public String latestInvoices(Model model) {

        final List<Invoice> latestInvoices = invoiceRepository.findFirst5ByOrderByDateDesc();

        // TODO should this be possible directly? see
        // TODO
        // https://docs.spring.io/spring-data/mongodb/docs/3.3.0/reference/html/#mapping-usage.document-references
        latestInvoices.forEach(invoice -> invoice.setCustomer(
                customerRepository.findById(invoice.getCustomerId())
                        .orElseThrow(() -> new IllegalStateException("customer not found"))));

        model.addAttribute("latestInvoices", latestInvoices);
        return "fragments/dashboard/latest-invoices :: latest-invoices";
    }

    @GetMapping("/dashboard/invoices")
    public String invoices(@RequestParam(required = false) final String query,
            @RequestParam(required = false, defaultValue = "1") final long page, final Locale locale,
            final Model model) {

        // TODO do we need totalItems here, this just displays the skeleton???
        final long totalItems;
        if (StringUtils.hasText(query)) {
            totalItems = invoiceRepository.countMatchingSearch(query, locale);
        } else {
            totalItems = invoiceRepository.count();
        }

        LOGGER.info("querying count for '{} ({})': {}", query, locale, totalItems);
        addPaginationAttributes(model, page, totalItems, INVOICES_PER_PAGE);

        return "dashboard/invoices";
    }

    @GetMapping("/dashboard/invoices/table")
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

        // TODO should this be possible directly? see
        // TODO
        // https://docs.spring.io/spring-data/mongodb/docs/3.3.0/reference/html/#mapping-usage.document-references
        invoices.forEach(invoice -> invoice.setCustomer(
                customerRepository.findById(invoice.getCustomerId())
                        .orElseThrow(() -> new IllegalStateException("customer not found"))));

        addPaginationAttributes(model, page, totalItems, INVOICES_PER_PAGE);
        model.addAttribute("invoices", invoices);

        return "fragments/invoices/table :: invoices-table";
    }

    @GetMapping("/dashboard/invoices/create")
    public String invoicesCreate(final Model model) {

        final List<BreadcrumbData> breadcrumbs = List.of(
                new BreadcrumbData("Invoices", "/dashboard/invoices", false),
                new BreadcrumbData("Create Invoice", "/dashboard/invoices/create", true));
        model.addAttribute("breadcrumbs", breadcrumbs);

        final List<Customer> customers = customerRepository.findAllByOrderByNameAsc(Pageable.unpaged());
        model.addAttribute("customers", customers);

        return "dashboard/invoices-create";
    }

    @PostMapping("/dashboard/invoices/create")
    public String createInvoice(final Invoice invoice) {

        // TODO create invoice
        return "dashboard/invoices";
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
