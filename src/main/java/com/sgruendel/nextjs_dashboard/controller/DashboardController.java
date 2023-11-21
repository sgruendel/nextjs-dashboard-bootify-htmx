package com.sgruendel.nextjs_dashboard.controller;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedList;
import java.util.List;

import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;
import com.sgruendel.nextjs_dashboard.ui.LinkData;

@Controller
public class DashboardController {

    //private final CustomersListener customersListener;

    private final CustomerRepository customerRepository;

    private final InvoiceRepository invoiceRepository;

    private final RevenueRepository revenueRepository;

    private final MongoOperations mongoOperations;

    public DashboardController(CustomerRepository customerRepository, InvoiceRepository invoiceRepository, RevenueRepository revenueRepository, MongoOperations mongoOperations) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.revenueRepository = revenueRepository;
        this.mongoOperations = mongoOperations;
    };

    @ModelAttribute("links")
    public List<LinkData> getLinks() {
        return List.of(
                new LinkData("Home", "/dashboard"), // TODO HomeIcon
                new LinkData("Invoices", "/dashboard/invoices"), // TODO DocumentDuplicateIcon
                new LinkData("Customers", "/dashboard/customers") // TODO UserGroupIcon
        );
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // TODO name of method/page
        return "dashboard/page";
    }

    @GetMapping("/dashboard/card")
    public String card(@RequestParam String icon, @RequestParam String title, @RequestParam String type) {
        final long value;
        switch (type) {
            case "collected":
                value = 164116;
                break;

            case "pending":
                value = 137932;
                break;

            case "customers":
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                value = customerRepository.count();
                break;

            case "invoices":
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                value = invoiceRepository.count();
                break;

            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
        return "fragments/dashboard/cards :: card(icon='" + icon + "', title='" + title + "', value=" + value + ")";
    }

    @GetMapping("/dashboard/revenue-chart")
    public String revenueChart(Model model) {
        //const revenues = await Revenues.find().lean().select(['month', 'revenue']).exec();
        List<Revenue> revenues = revenueRepository.findAll();
        if (revenues.isEmpty()) {
            return "fragments/dashboard/revenue-chart :: revenue-chart-empty";
        }

        model.addAttribute("revenues", revenues);

        // Calculate what labels we need to display on the y-axis
        // based on highest record and in 1000s
        /*
            const highestRecord = Math.max(...revenue.map((month) => month.revenue));
            const topLabel = Math.ceil(highestRecord / 1000) * 1000;

            for (let i = topLabel; i >= 0; i -= 1000) {
                yAxisLabels.push(`$${i / 1000}K`);
            }
         */
        //TODO Integer highestRevenue = revenues.stream().max((r1, r2) -> r1.getRevenue() > r2.getRevenue()).get().getRevenue();
        final int highestRevenue = 4800;
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
        return "fragments/dashboard/latest-invoices :: latest-invoices";
    }
}
