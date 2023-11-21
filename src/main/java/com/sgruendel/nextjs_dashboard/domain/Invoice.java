package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document("invoices")
@Getter
@Setter
public class Invoice {

    @Id
    private ObjectId id;

    @NotNull
    private ObjectId customerId;

    @NotNull
    private Integer amount;

    @NotNull
    @Size(max = 255)
    private String status;

    @NotNull
    private LocalDate date;

    @DocumentReference(lazy = true)
    private Customer customer;

}
