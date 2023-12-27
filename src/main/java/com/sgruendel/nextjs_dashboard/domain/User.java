package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(User.COLLECTION_NAME)
@Getter
@Setter
public class User {

    public final static String COLLECTION_NAME = "users";

    // no @NotNull so MongoDB autogenerates id
    @Id
    private String id;

    @NotNull
    @Size(max = 128)
    private String name;

    @Indexed(unique = true)
    @NotNull
    @Size(max = 128)
    private String email;

    @NotNull
    @Size(max = 72)
    private String password;

    @CreatedDate
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    private OffsetDateTime lastUpdated;

    @Version
    private Integer version;

}
