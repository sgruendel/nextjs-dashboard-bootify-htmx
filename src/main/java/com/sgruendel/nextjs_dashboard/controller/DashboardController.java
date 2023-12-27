package com.sgruendel.nextjs_dashboard.controller;

import com.sgruendel.nextjs_dashboard.model.InvoiceDTO;
import com.sgruendel.nextjs_dashboard.model.RevenueDTO;
import com.sgruendel.nextjs_dashboard.model.Status;
import com.sgruendel.nextjs_dashboard.service.CustomerService;
import com.sgruendel.nextjs_dashboard.service.InvoiceService;
import com.sgruendel.nextjs_dashboard.service.RevenueService;
import com.sgruendel.nextjs_dashboard.ui.CardData;
import com.sgruendel.nextjs_dashboard.util.WebUtils;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasAuthority('ROLE_USER')")
@RequestMapping("/dashboard")
public class DashboardController {

    private final CustomerService customerService;

    private final InvoiceService invoiceService;

    private final RevenueService revenueService;

    public DashboardController(CustomerService customerService, InvoiceService invoiceService,
            RevenueService revenueService) {

        this.customerService = customerService;
        this.invoiceService = invoiceService;
        this.revenueService = revenueService;
    }

    @GetMapping
    public String dashboard() {
        return "dashboard/index";
    }

    @GetMapping("/cards")
    public String cards(final Model model) throws InterruptedException {
        // Thread.sleep(3000);

        final Map<String, Long> sumOfAmountByStatus = invoiceService.sumAmountGroupByStatus();

        final List<CardData> cards = List.of(
                new CardData<>("banknotes-icon", "Collected",
                        WebUtils.formatCurrency(sumOfAmountByStatus.get(Status.PAID.name()))),
                new CardData<>("clock-icon", "Pending",
                        WebUtils.formatCurrency(sumOfAmountByStatus.get(Status.PENDING.name()))),
                new CardData<>("inbox-icon", "Total Invoices", invoiceService.count()),
                new CardData<>("user-group-icon", "Total Customers", customerService.count()));

        model.addAttribute("cards", cards);
        return "fragments/dashboard/cards :: cards";
    }

    @GetMapping("/revenue-chart")
    public String revenueChart(Model model) throws InterruptedException {
        // Thread.sleep(3000);

        // const revenues = await Revenues.find().lean().select(['month',
        // 'revenue']).exec();
        List<RevenueDTO> revenues = revenueService.findAll();
        if (revenues.isEmpty()) {
            return "fragments/dashboard/revenue-chart :: revenue-chart-empty";
        }

        model.addAttribute("revenues", revenues);

        // Calculate what labels we need to display on the y-axis
        // based on highest record and in 1000s
        @SuppressWarnings("OptionalGetWithoutIsPresent") // it's not empty
        final int highestRevenue = revenues.stream().max(Comparator.comparingInt(RevenueDTO::getRevenue)).get()
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

        final List<InvoiceDTO> latestInvoices = invoiceService.findFirst5ByOrderByDateDesc();

        model.addAttribute("latestInvoices", latestInvoices);
        return "fragments/dashboard/latest-invoices :: latest-invoices";
    }

}
