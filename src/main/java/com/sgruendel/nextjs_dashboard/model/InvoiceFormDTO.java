package com.sgruendel.nextjs_dashboard.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Like InvoiceDTO but has a customer reference via it's id, used for
 * create/edit invoice.
 */
@Getter
@Setter
public class InvoiceFormDTO {

    @Size(max = 64)
    private String id;

    @NotNull
    private Double amount;

    @NotNull
    private Status status;

    @NotNull
    private LocalDateTime date;

    @Size(max = 64)
    private String customer;

}
