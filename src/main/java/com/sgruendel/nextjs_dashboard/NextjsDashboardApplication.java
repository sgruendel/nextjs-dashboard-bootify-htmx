package com.sgruendel.nextjs_dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.mongock.runner.springboot.EnableMongock;

@SpringBootApplication
@EnableMongock
public class NextjsDashboardApplication {

    public static void main(final String[] args) {
        SpringApplication.run(NextjsDashboardApplication.class, args);
    }

}
