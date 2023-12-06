package com.sgruendel.nextjs_dashboard.domain;

import com.sgruendel.nextjs_dashboard.model.Status;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Document(Invoice.COLLECTION_NAME)
@Getter
@Setter
public class Invoice {

    public final static String COLLECTION_NAME = "invoices";

    // no @NotNull so MongoDB autogenerates id
    @Id
    private String id;

    @NotNull
    private Integer amount;

    @NotNull
    private Status status;

    @Indexed
    @NotNull
    private LocalDateTime date;

    @DocumentReference(lazy = true)
    private Customer customer;

    @CreatedDate
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    private OffsetDateTime lastUpdated;

    @Version
    private Integer version;

}
