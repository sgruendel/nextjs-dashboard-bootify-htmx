package com.sgruendel.nextjs_dashboard.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("users")
@Getter
@Setter
public class User {

    @Id
    private UUID id;

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String password;

}
