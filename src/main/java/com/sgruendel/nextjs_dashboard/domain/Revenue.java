package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(Revenue.COLLECTION_NAME)
@Getter
@Setter
public class Revenue {

    public final static String COLLECTION_NAME = "revenues";

    @NotNull
    @Id
    private String month;

    @NotNull
    private Integer revenue;

    @CreatedDate
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    private OffsetDateTime lastUpdated;

    @Version
    private Integer version;

}
