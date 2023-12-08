package com.sgruendel.nextjs_dashboard.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerWithTotalsDTO {

    @Size(max = 64)
    private String id;

    @NotNull
    @Size(max = 128)
    private String name;

    @NotNull
    @Size(max = 128)
    private String email;

    @NotNull
    @Size(max = 64)
    private String imageUrl;

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
