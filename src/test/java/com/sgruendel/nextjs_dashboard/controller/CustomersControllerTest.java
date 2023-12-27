package com.sgruendel.nextjs_dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class CustomersControllerTest extends BaseControllerTest {

    @Test
    void testCustomersNotLoggedIn() throws IOException {

        final HtmlPage page = webClient.getPage(BASE_URL + "/dashboard/customers");
        assertEquals(BASE_URL + "/login?loginRequired=true", page.getBaseURI());
    }

    @Test
    @WithMockUser
    void testCustomers() throws IOException {

        final HtmlPage page = webClient.getPage(BASE_URL + "/dashboard/customers");
        assertEquals("Customers | Acme Dashboard", page.getTitleText());

        final HtmlTextInput search = page.getHtmlElementById("search");
        assertEquals("#customers-table", search.getAttribute("hx-target"));

        assertNotNull(page.getElementById("customers-table"));
    }

    @Test
    @WithMockUser
    void testCustomersTable() throws IOException {
        final HtmlPage fragment = webClient.getPage(BASE_URL + "/dashboard/customers/table");

        assertNotNull(fragment.getElementById("customers-table"));
    }

}
