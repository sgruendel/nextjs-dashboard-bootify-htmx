package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class CustomerWithTotals extends Customer {

    @NotNull
    @Min(0)
    private Long totalInvoices;

    @NotNull
    @Min(0)
    private Long totalPending;

    @NotNull
    @Min(0)
    private Long totalPaid;
}
