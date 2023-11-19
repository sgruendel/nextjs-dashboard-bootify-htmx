package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document("invoiceses")
@Getter
@Setter
public class Invoices {

    @Id
    private UUID id;

    @NotNull
    private UUID customerId;

    @NotNull
    private Integer amount;

    @NotNull
    @Size(max = 255)
    private String status;

    @NotNull
    private LocalDate date;

    @DocumentReference(lazy = true)
    private Customers customer;

}
