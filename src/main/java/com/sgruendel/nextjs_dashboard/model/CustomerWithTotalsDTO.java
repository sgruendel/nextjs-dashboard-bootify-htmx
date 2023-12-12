package com.sgruendel.nextjs_dashboard.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerWithTotalsDTO extends CustomerDTO {

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
