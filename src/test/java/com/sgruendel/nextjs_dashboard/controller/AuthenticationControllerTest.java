package com.sgruendel.nextjs_dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class AuthenticationControllerTest extends BaseControllerTest {

    @Test
    void testLogin() throws IOException {
        final HtmlPage page = webClient.getPage(BASE_URL + "/login");
        assertEquals("Login | Acme Dashboard", page.getTitleText());
        assertEquals("/login", page.getForms().get(0).getActionAttribute());
    }
}
