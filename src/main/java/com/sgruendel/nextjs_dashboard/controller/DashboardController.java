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
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private static final List<LinkData> LINKS = List.of(
            new LinkData("Home", "/dashboard", "home-icon", false),
            new LinkData("Invoices", "/dashboard/invoices", "document-duplicate-icon", false),
            new LinkData("Customers", "/dashboard/customers", "user-group-icon", false));

    private static final int INVOICES_PER_PAGE = 6;

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
    public String invoices(@RequestParam(required = false) final String query,
            @RequestParam(required = false, defaultValue = "1") final int page, final Model model) {

        final long totalItems =
                StringUtils.hasText(query)
                        ? mongoOperations.count(getFilteredInvoicesQuery(query), "invoices")
                        : invoiceRepository.count();
        LOGGER.info("querying count for '{}': {}", query, totalItems);

        addPaginationAttributes(model, page, totalItems, INVOICES_PER_PAGE);

        return "dashboard/invoices";
    }

    @GetMapping("/dashboard/invoices/table")
    public String invoicesTable(final HttpServletResponse response, @RequestParam(required = false) final String query,
            @RequestParam final int page, final Model model) throws InterruptedException {

        LOGGER.info("querying invoices for '{}'", query);

        final String queryLower = StringUtils.hasLength(query) ? query.toLowerCase() : "";

        final LookupOperation invoicesLookup = Aggregation.lookup()
                .from("customers")
                .localField("customer_id")
                .foreignField("_id")
                /*
                .let(newVariable("invoice_amount").forExpression(AggregationExpression.from(MongoExpression.create("$amount"))), // TODO $toString $amount
                        // TODO newVariable("invoice_date"),
                        newVariable("invoice_status").forExpression(AggregationExpression.from(MongoExpression.create("$status"))))

                .pipeline(AggregationPipeline.of(
                        new MatchOperation(
                                BooleanOperators.Or.or(
                                        ComparisonOperators.Gte.valueOf(
                                                StringOperators.IndexOfCP.valueOf("$name").indexOf(query) // TODO $toLower $name
                                        ).greaterThanEqualToValue(0),
                                        ComparisonOperators.Gte.valueOf(
                                                StringOperators.IndexOfCP.valueOf("$email").indexOf(query) // TODO $toLower $email
                                        ).greaterThanEqualToValue(0)
                                )
                        )
                ))
                 */
                .as("customer");

        final UnwindOperation unwind = Aggregation.unwind("$customer");
        final SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "date");
        final ProjectionOperation project = Aggregation.project("_id", "_customer", "date", "amount", "status");
        final Aggregation aggregation = Aggregation.newAggregation(invoicesLookup, unwind, sort, project);
        //final AggregationResults<Invoice> ags = mongoOperations.aggregate(aggregation, "invoices", Invoice.class);

        final List<Invoice> invoices = mongoOperations.find(
                getFilteredInvoicesQuery(query)
                        .with(Sort.by(Sort.Direction.DESC, "date"))
                        .skip(((long) page - 1) * INVOICES_PER_PAGE)
                        .limit(INVOICES_PER_PAGE),
                Invoice.class,
                "invoices");

        // TODO fetchFilteredInvoices(query, page, itemsPerPage);
        // TODO calc
        //final Page<Invoice> invoicePages = invoiceRepository.findAll(Pageable.ofSize((itemsPerPage)));

        /*
        final List<Invoice> invoices = invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "date")).subList(0,
                itemsPerPage);

         */

        // TODO should this be possible directly? see
        // TODO
        // https://docs.spring.io/spring-data/mongodb/docs/3.3.0/reference/html/#mapping-usage.document-references
        invoices.forEach(invoice -> invoice.setCustomer(
                customerRepository.findById(invoice.getCustomerId())
                        .orElseThrow(() -> new IllegalStateException("customer not found"))));

        Thread.sleep(1000);
        addPaginationAttributes(model, page,
                mongoOperations.count(getFilteredInvoicesQuery(query), "invoices"),
                INVOICES_PER_PAGE);
        model.addAttribute("invoices", invoices);

        if (query != null) {
            // add query params to URL
            response.addHeader("HX-Replace-Url", "?query=" + query + "&page=" + page);
        }
        return "fragments/invoices/table :: invoices-table";
    }

    private List<PaginationData> createPaginations(final int page, final int totalPages) {
        final List<String> texts;

        // If the total number of pages is 7 or less,
        // display all pages without any ellipsis.
        if (totalPages <= 7) {
            texts = IntStream.rangeClosed(1, totalPages).boxed().map(Object::toString).collect(Collectors.toList());
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

    // TODO move to invoicesRepo?
    private Query getFilteredInvoicesQuery(final String query) {
        // TODO if query null?
        final Criteria criteria = Criteria.
                where("status").regex(".*" + (query == null ? "" :query) + ".*", "i");
        // TODO .orOperator()
        return Query.query(criteria);
    }

    private void addPaginationAttributes(final Model model, final int page, final long totalItems, final int itemsPerPage) {
        final int startIndex = (page - 1) * itemsPerPage + 1;
        final int endIndex = Long.valueOf(Math.min((long) page * itemsPerPage, totalItems)).intValue();
        final int totalPages = Double.valueOf(Math.ceil((double) totalItems / itemsPerPage)).intValue();
        LOGGER.info("pagination attributes from page={}, total items={}: start index={}, end index={}, total pages={}",
                page, totalItems, startIndex, endIndex, totalPages);

        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("paginations", createPaginations(page, totalPages));
    }
}
