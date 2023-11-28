package com.sgruendel.nextjs_dashboard.controller;

import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;
import com.sgruendel.nextjs_dashboard.ui.LinkData;
import com.sgruendel.nextjs_dashboard.ui.PaginationData;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public String invoices(@RequestParam(required = false) String query,
                           @RequestParam(required = false, defaultValue = "1") int currentPage, Model model) {
        // TODO totalItems = await fetchFilteredInvoicesCount(query);
        long totalItems = invoiceRepository.count();

        //final int currentPageAsInt = 1;//Integer.parseInt(currentPage);

        // TODO also in table.html
        final int itemsPerPage = 6;
        final int startIndex = (currentPage - 1) * itemsPerPage + 1;
        final int endIndex = Long.valueOf(Math.min((long) currentPage * itemsPerPage, totalItems)).intValue();
        final int totalPages = Long.valueOf(totalItems / itemsPerPage).intValue();

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("paginations", createPaginations(currentPage, totalPages));
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

        return "fragments/invoices/table :: invoices-table";
    }

    private List<PaginationData> createPaginations(final int currentPage, final int totalPages) {
        final List<String> texts;

        // If the total number of pages is 7 or less,
        // display all pages without any ellipsis.
        if (totalPages <= 7) {
            texts = IntStream.rangeClosed(1, totalPages).boxed().map(Object::toString).collect(Collectors.toList());
        } else

        // If the current page is among the first 3 pages,
        // show the first 3, an ellipsis, and the last 2 pages.
        if (currentPage <= 3) {
            texts = List.of("1", "2", "3", "...", String.valueOf(totalPages - 1), String.valueOf(totalPages));
        } else

        // If the current page is among the last 3 pages,
        // show the first 2, an ellipsis, and the last 3 pages.
        if (currentPage >= totalPages - 2) {
            texts = List.of("1", "2", "...", String.valueOf(totalPages - 2), String.valueOf(totalPages - 1),
                    String.valueOf(totalPages));
        } else

        // If the current page is somewhere in the middle,
        // show the first page, an ellipsis, the current page and its neighbors,
        // another ellipsis, and the last page.
        {
            texts = List.of("1", "...", String.valueOf(currentPage - 1), String.valueOf(currentPage),
                    String.valueOf(currentPage + 1), "...", String.valueOf(totalPages));
        }

        final List<PaginationData> paginations = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i++) {
            final String text = texts.get(i);
            String position = "";
            if (i == 0) position = "first";
            if (i == texts.size() - 1) position = "last";
            if (texts.size() == 1) position = "single";
            if (text.equals("...")) position = "middle";

            paginations.add(new PaginationData(text, position));
        }
        return paginations;
    }

}
