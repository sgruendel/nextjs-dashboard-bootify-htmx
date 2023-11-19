package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document("customerses")
@Getter
@Setter
public class Customers {

    @Id
    private UUID id;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 255)
    private String imageUrl;

    @DocumentReference(lazy = true, lookup = "{ 'customer' : ?#{#self._id} }")
    @ReadOnlyProperty
    private Set<Invoices> invoice;

}
