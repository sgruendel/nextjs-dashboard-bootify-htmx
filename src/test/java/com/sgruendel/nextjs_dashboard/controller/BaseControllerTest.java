package com.sgruendel.nextjs_dashboard.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public abstract class BaseControllerTest {

    protected final static String BASE_URL = "http://localhost:8081";

    protected WebClient webClient;

    @BeforeEach
    void setup(final WebApplicationContext context) {
        webClient = MockMvcWebClientBuilder
                .webAppContextSetup(context, SecurityMockMvcConfigurers.springSecurity())
                .build();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }

}
