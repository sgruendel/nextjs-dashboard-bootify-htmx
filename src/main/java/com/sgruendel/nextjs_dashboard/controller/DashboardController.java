package com.sgruendel.nextjs_dashboard.controller;

import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;
import com.sgruendel.nextjs_dashboard.ui.LinkData;
import com.sgruendel.nextjs_dashboard.util.WebUtils;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Controller
public class DashboardController {

    private static final List<LinkData> LINKS = List.of(
            new LinkData("Home", "/dashboard", "home-icon", false),
            new LinkData("Invoices", "/dashboard/invoices", "document-duplicate-icon", false),
            new LinkData("Customers", "/dashboard/customers", "user-group-icon", false));

    private final CustomerRepository customerRepository;

    private final InvoiceRepository invoiceRepository;

    private final RevenueRepository revenueRepository;

    private final MongoOperations mongoOperations;

    public DashboardController(CustomerRepository customerRepository, InvoiceRepository invoiceRepository,
            RevenueRepository revenueRepository, MongoOperations mongoOperations) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.revenueRepository = revenueRepository;
        this.mongoOperations = mongoOperations;
    }

    @ModelAttribute("links")
    public List<LinkData> getLinks(final HttpServletRequest request) {
        final String requestURIasString = request.getRequestURI().toString();
        final List<LinkData> links = new LinkedList<>();
        LINKS.forEach(link -> {
            links.add(new LinkData(link.getName(), link.getHref(), link.getIcon(),
                    link.getHref().equals(requestURIasString)));
        });
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
                // TODO get real value
                value = WebUtils.formatCurrency(164116);
                break;

            case "pending":
                // TODO get real value
                value = WebUtils.formatCurrency(137932);
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
        // TODO implement proper query
        final List<Invoice> latestInvoices = invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "date")).subList(0,
                5);

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
    public String invoices(Model model) {
        // TODO name of method/page
        int currentPage = 1;
        int itemsPerPage = 6;
        int totalItems = 42;

        final int startIndex = (currentPage - 1) * itemsPerPage + 1;
        final int endIndex = Math.min(currentPage * itemsPerPage, totalItems);

        final int totalPages = totalItems / itemsPerPage;

        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("pagination", createPagination(currentPage, totalPages));
        return "dashboard/invoices";
    }

    @GetMapping("/dashboard/invoices/table")
    public String invoicesTable(@RequestParam(required = false) String query, @RequestParam int currentPage,
            @RequestParam int itemsPerPage, Model model) throws InterruptedException {
        // TODO fetchFilteredInvoices(query, currentPage, itemsPerPage);
        // TODO calc
        final List<Invoice> invoices = invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "date")).subList(0,
                itemsPerPage);

        // TODO should this be possible directly? see
        // TODO
        // https://docs.spring.io/spring-data/mongodb/docs/3.3.0/reference/html/#mapping-usage.document-references
        invoices.forEach(invoice -> invoice.setCustomer(
                customerRepository.findById(invoice.getCustomerId())
                        .orElseThrow(() -> new IllegalStateException("customer not found"))));

        Thread.sleep(1000);
        model.addAttribute("invoices", invoices);

        return "fragments/invoices/table :: table (currentPage=" + currentPage + ", itemsPerPage=" + itemsPerPage + ")";
    }

    private List<String> createPagination(final int currentPage, final int totalPages) {
        // If the total number of pages is 7 or less,
        // display all pages without any ellipsis.
        if (totalPages <= 7) {
            final List<String> pagination = new ArrayList<>();
            for (int i = 1; i <= totalPages; i++) {
                pagination.add(String.valueOf(i));
            }
            return pagination;

            // return ArrayUtils.toObject(IntStream.rangeClosed(1,
            // totalPages).boxed().collect(Collectors.toList()));
            // TODO return List.of(IntStream.rangeClosed(currentPage,
            // totalPages).boxed().collect(Collectors.toList());
            // TODO return Array.from({ length: totalPages }, (_, i) => i + 1);
        }

        // If the current page is among the first 3 pages,
        // show the first 3, an ellipsis, and the last 2 pages.
        if (currentPage <= 3) {
            return List.of("1", "2", "3", "...", String.valueOf(totalPages - 1), String.valueOf(totalPages));
        }

        // If the current page is among the last 3 pages,
        // show the first 2, an ellipsis, and the last 3 pages.
        if (currentPage >= totalPages - 2) {
            return List.of("1", "2", "...", String.valueOf(totalPages - 2), String.valueOf(totalPages - 1),
                    String.valueOf(totalPages));
        }

        // If the current page is somewhere in the middle,
        // show the first page, an ellipsis, the current page and its neighbors,
        // another ellipsis, and the last page.
        return List.of("1", "...", String.valueOf(currentPage - 1), String.valueOf(currentPage),
                String.valueOf(currentPage + 1), "...", String.valueOf(totalPages));
    };

}
