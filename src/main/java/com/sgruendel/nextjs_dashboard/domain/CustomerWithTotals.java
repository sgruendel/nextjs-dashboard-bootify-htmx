package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
public class CustomerWithTotals {

    @NotNull
    @Id
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
