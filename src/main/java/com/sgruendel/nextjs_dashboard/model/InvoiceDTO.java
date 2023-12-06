package com.sgruendel.nextjs_dashboard.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDTO {

    @Size(max = 64)
    private String id;

    @NotNull
    private Integer amount;

    @NotNull
    private Status status;

    @NotNull
    private LocalDateTime date;

    @Size(max = 64)
    private String customer;

}
