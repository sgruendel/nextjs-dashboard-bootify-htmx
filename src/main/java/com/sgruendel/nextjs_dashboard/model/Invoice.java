package com.sgruendel.nextjs_dashboard.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Invoice {

    private Long amount;
    private Status status;
    private LocalDateTime date;

}
