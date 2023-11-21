package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("revenues")
@Getter
@Setter
public class Revenue {

    // TODO @Id

    @NotNull
    private String month;

    @NotNull
    private Integer revenue;

}
