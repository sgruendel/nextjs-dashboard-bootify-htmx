package com.sgruendel.nextjs_dashboard.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Like InvoiceDTO but has a customer reference via its id and amount in cents,
 * used for create/edit invoice forms.
 */
@Getter
@Setter
public class InvoiceFormDTO {

    @Size(max = 64)
    private String id;

    @NotNull(message = "Please enter an amount.")
    @Positive(message = "Please enter an amount greater than $0.")
    private Double amount;

    @NotNull(message = "Please select an invoice status.")
    private Status status;

    // no need for a NotNull message, automatically set by controller
    @NotNull
    private LocalDateTime date;

    @NotNull(message = "Please select a customer.")
    // no need for a Size message, all ids are max. 64 characters
    @Size(max = 64)
    private String customer;

}
