package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.OffsetDateTime;
import java.util.Set;

@Document(Customer.COLLECTION_NAME)
@Getter
@Setter
public class Customer {

    public final static String COLLECTION_NAME = "customers";

    // no @NotNull so MongoDB autogenerates id
    @Id
    private String id;

    @Indexed
    @NotNull
    @Size(max = 128)
    private String name;

    @Indexed(unique = true)
    @NotNull
    @Size(max = 128)
    private String email;

    @NotNull
    @Size(max = 64)
    private String imageUrl;

    @DocumentReference(lazy = true, lookup = "{ 'customer' : ?#{#self._id} }")
    @ReadOnlyProperty
    private Set<Invoice> invoices;

    @CreatedDate
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    private OffsetDateTime lastUpdated;

    @Version
    private Integer version;

}
