package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Set;


@Document("customers")
@Getter
@Setter
public class Customer {

    @Id
    private String id;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 255)
    @Field("image_url")
    private String imageUrl;

    @ReadOnlyProperty
    @DocumentReference(lazy = true, lookup = "{ 'customer_id' : ?#{#self._id} }")
    private Set<Invoice> invoice;

}
