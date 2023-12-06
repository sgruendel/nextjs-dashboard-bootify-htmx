package com.sgruendel.nextjs_dashboard.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevenueDTO {

    @Size(max = 4)
    private String month;

    @NotNull
    private Integer revenue;

}
