package com.sgruendel.nextjs_dashboard.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mongounit.LocationType;
import org.mongounit.MongoUnitTest;
import org.mongounit.SeedWithDataset;
import org.springframework.security.test.context.support.WithMockUser;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@MongoUnitTest
@SeedWithDataset(locationType = LocationType.CLASSPATH_ROOT)
public class InvoicesControllerTest extends BaseControllerTest {

    @Test
    void testInvoicesNotLoggedIn() throws IOException, URISyntaxException {

        final HtmlPage page = webClient.getPage(BASE_URL + "/dashboard/invoices");
        assertEquals(BASE_URL + "/login?loginRequired=true", page.getBaseURI());
    }

    @Test
    @WithMockUser
    void testInvoices() throws IOException {

        final HtmlPage page = webClient.getPage(BASE_URL + "/dashboard/invoices");
        assertEquals("Invoices | Acme Dashboard", page.getTitleText());

        final HtmlTextInput search = page.getHtmlElementById("search");
        assertEquals("#invoices-table", search.getAttribute("hx-target"));
    }

    @Test
    @WithMockUser
    void testInvoicesTable() throws IOException {
        final HtmlPage fragment = webClient.getPage(BASE_URL + "/dashboard/invoices/table");

        assertNotNull(fragment.getElementById("invoices-table"));
        assertEquals("/images/customers/delba-de-oliveira.png",
                fragment.getElementById("customer-image-url-1").getAttribute("src"));
        assertEquals("Delba de Oliveira", fragment.getElementById("customer-name-1").asNormalizedText());
        assertEquals("delba@oliveira.com", fragment.getElementById("customer-email-1").asNormalizedText());
        assertEquals("$89.45", fragment.getElementById("amount-1").asNormalizedText());
        assertEquals("Oct 4, 2023", fragment.getElementById("date-1").asNormalizedText());
        assertEquals("Paid", fragment.getElementById("status-1").asNormalizedText());
    }

    @Test
    @WithMockUser
    void givenEmptyPage_whenCreatingInvoice_thenAllErrorsShown() throws IOException {

        // given empty page
        final HtmlPage page = webClient.getPage(BASE_URL + "/dashboard/invoices/create");
        assertEquals("Create Invoice | Acme Dashboard", page.getTitleText());

        // when creating invoice
        final HtmlForm invoiceForm = page.getFormByName("invoice-form");
        final HtmlButton submitButton = invoiceForm.getButtonByName("createInvoice");
        final HtmlPage resultPage = submitButton.click();

        // then all errors are shown
        final DomElement customerError = resultPage.getElementById("customer-error");
        assertEquals("Please select a customer.", customerError.getTextContent().trim());

        final DomElement amountError = resultPage.getElementById("amount-error");
        assertEquals("Please enter an amount.", amountError.getTextContent().trim());

        final DomElement statusError = resultPage.getElementById("status-error");
        assertEquals("Please select an invoice status.", statusError.getTextContent().trim());

        final DomElement anyErrors = resultPage.getElementById("any-errors");
        assertEquals("Missing Fields. No invoice created.", anyErrors.getTextContent().trim());
    }

    @Test
    @WithMockUser
    void givenValidPage_whenCreatingInvoice_thenInvoicesPageShown() throws IOException {

        // given valid page
        final HtmlPage page = webClient.getPage(BASE_URL + "/dashboard/invoices/create");
        assertEquals("Create Invoice | Acme Dashboard", page.getTitleText());

        final HtmlForm invoiceForm = page.getFormByName("invoice-form");
        final HtmlSelect customerSelect = invoiceForm.getSelectByName("customerId");
        assertEquals(11, customerSelect.getOptionSize());
        customerSelect.getOptionByText("Evil Rabbit").setSelected(true);

        invoiceForm.getInputByName("amount").setValue("123");
        invoiceForm.getInputByValue("PAID").setChecked(true);

        // when creating invoice
        final HtmlButton submitButton = invoiceForm.getButtonByName("createInvoice");
        final HtmlPage resultPage = submitButton.click();

        // then Invoices page is shown
        assertEquals("Invoices | Acme Dashboard", resultPage.getTitleText());

        // TODO this should have been loaded automatically via htmx hx-trigger="load"
        // but currently doesn't because of missing JS parsing in rhino, see
        // https://github.com/mozilla/rhino/issues/678
        final HtmlPage fragment = webClient.getPage(BASE_URL + "/dashboard/invoices/table");
        assertEquals("/images/customers/evil-rabbit.png",
                fragment.getElementById("customer-image-url-1").getAttribute("src"));
        assertEquals("Evil Rabbit", fragment.getElementById("customer-name-1").asNormalizedText());
        assertEquals("evil@rabbit.com", fragment.getElementById("customer-email-1").asNormalizedText());
        assertEquals("$123.00", fragment.getElementById("amount-1").asNormalizedText());
        assertEquals(DateTimeFormatter.ofPattern("MMM d, yyyy").format(LocalDate.now()),
                fragment.getElementById("date-1").asNormalizedText());
        assertEquals("Paid", fragment.getElementById("status-1").asNormalizedText());
    }

    @Test
    @WithMockUser
    void givenExistingInvoice_whenEditInvoice_thenInvoiceShown() throws IOException {

        // given existing invoice
        final String url = BASE_URL + "/dashboard/invoices/edit/6589801ac4f5da26efad28c2";

        // when editing invoice
        final HtmlPage page = webClient.getPage(url);

        // then invoice is shown
        assertEquals("Edit Invoice | Acme Dashboard", page.getTitleText());
        final HtmlForm invoiceForm = page.getFormByName("invoice-form");
        final HtmlSelect customerSelect = invoiceForm.getSelectByName("customerId");
        assertEquals(11, customerSelect.getOptionSize());
        assertTrue(customerSelect.getOptionByText("Michael Novotny").isSelected());
        assertEquals("345.77", invoiceForm.getInputByName("amount").getValue());
        assertTrue(invoiceForm.getInputByValue("PENDING").isChecked());
    }

    @Test
    @WithMockUser
    void givenNonExistingInvoice_whenEditInvoice_thenFailing404() throws IOException {

        // given non-existing invoice
        final String url = BASE_URL + "/dashboard/invoices/edit/non-existing-invoice";

        // when editing invoice
        FailingHttpStatusCodeException failing404 = assertThrows(FailingHttpStatusCodeException.class,
                () -> webClient.getPage(url));

        // then 404 Not Found is returned
        assertEquals(HttpStatus.SC_NOT_FOUND, failing404.getResponse().getStatusCode());
    }

    @Test
    @WithMockUser
    void givenExistingInvoice_whenDeleteInvoice_thenInvoiceShown() throws IOException {

        // given existing invoice
        final HtmlPage fragment = webClient.getPage(BASE_URL + "/dashboard/invoices/table");
        assertEquals("Delba de Oliveira", fragment.getElementById("customer-name-1").getTextContent());
        System.out.println(fragment.asXml());

        // when deleting invoice
        final HtmlForm deleteForm = fragment.getFormByName("delete-invoice-1");
        final HtmlButton deleteButton = deleteForm.getButtonByName("delete-invoice-1");

        // TODO HtmlUnit doesn't send a DELETE request here but GET
        //final HtmlPage resultPage = deleteButton.click();
    }

}
