package com.sgruendel.nextjs_dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class HomeControllerTest extends BaseControllerTest {

    @Test
    void testIndexWithoutUser() throws IOException {
        final HtmlPage page = webClient.getPage(BASE_URL + "/");
        assertEquals("Acme Dashboard", page.getTitleText());
        assertNotNull(page.getAnchorByHref("/login"));
    }

    @Test
    @WithMockUser
    void testIndexWithUser() throws IOException {
        final HtmlPage page = webClient.getPage(BASE_URL + "/");
        System.out.print(page.asXml());
        assertEquals("Dashboard | Acme Dashboard", page.getTitleText());
        assertNotNull(page.getFormByName("logout-form"));
    }

}
