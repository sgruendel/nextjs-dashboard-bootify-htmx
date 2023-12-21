package com.sgruendel.nextjs_dashboard.migration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.model.IndexOptions;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;
import com.sgruendel.nextjs_dashboard.repos.InvoiceRepository;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "invoice-initializer", order = "3", author = "sgruendel")
public class InvoiceInitializerChangeUnit {

  private final MongoTemplate mongoTemplate;

  private final InvoiceRepository invoiceRepository;

  private final CustomerRepository customerRepository;

  public InvoiceInitializerChangeUnit(MongoTemplate mongoTemplate, InvoiceRepository invoiceRepository,
      CustomerRepository customerRepository) {

    this.mongoTemplate = mongoTemplate;
    this.invoiceRepository = invoiceRepository;
    this.customerRepository = customerRepository;
  }

  @BeforeExecution
  public void beforeExecution() {
    mongoTemplate.createCollection(Invoice.COLLECTION_NAME)
        .createIndex(new Document("date", -1), new IndexOptions().name("date"));
  }

  @RollbackBeforeExecution
  public void rollbackBeforeExecution() {
    mongoTemplate.dropCollection(Invoice.COLLECTION_NAME);
  }

  @Execution
  public void execution() throws IOException {

    invoiceRepository.saveAll(getInvoices());
  }

  @RollbackExecution
  public void rollbackExecution() {

    invoiceRepository.deleteAll();
  }

  private List<Invoice> getInvoices() throws IOException {

    // customer reference stored in version
    final String jsonData = """
                [
                  {
                    "version": 0,
                    "amount": 15795,
                    "status": "PENDING",
                    "date": "2022-12-06T00:00:00"
                  },
                  {
                    "version": 1,
                    "amount": 20348,
                    "status": "PENDING",
                    "date": "2022-11-14T00:00:00"
                  },
                  {
                    "version": 4,
                    "amount": 3040,
                    "status": "PAID",
                    "date": "2022-10-29T00:00:00"
                  },
                  {
                    "version": 3,
                    "amount": 44800,
                    "status": "PAID",
                    "date": "2023-09-10T00:00:00"
                  },
                  {
                    "version": 5,
                    "amount": 34577,
                    "status": "PENDING",
                    "date": "2023-08-05T00:00:00"
                  },
                  {
                    "version": 7,
                    "amount": 54246,
                    "status": "PENDING",
                    "date": "2023-07-16T00:00:00"
                  },
                  {
                    "version": 6,
                    "amount": 666,
                    "status": "PENDING",
                    "date": "2023-06-27T00:00:00"
                  },
                  {
                    "version": 3,
                    "amount": 32545,
                    "status": "PAID",
                    "date": "2023-06-09T00:00:00"
                  },
                  {
                    "version": 4,
                    "amount": 1250,
                    "status": "PAID",
                    "date": "2023-06-17T00:00:00"
                  },
                  {
                    "version": 5,
                    "amount": 8546,
                    "status": "PAID",
                    "date": "2023-06-07T00:00:00"
                  },
                  {
                    "version": 1,
                    "amount": 500,
                    "status": "PAID",
                    "date": "2023-08-19T00:00:00"
                  },
                  {
                    "version": 5,
                    "amount": 8945,
                    "status": "PAID",
                    "date": "2023-06-03T00:00:00"
                  },
                  {
                    "version": 2,
                    "amount": 8945,
                    "status": "PAID",
                    "date": "2023-06-18T00:00:00"
                  },
                  {
                    "version": 0,
                    "amount": 8945,
                    "status": "PAID",
                    "date": "2023-10-04T00:00:00"
                  },
                  {
                    "version": 2,
                    "amount": 1000,
                    "status": "PAID",
                    "date": "2022-06-05T00:00:00"
                  }
                ]
        """;
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    // Deserialize JSON array to a list of Invoice objects
    Invoice[] invoices = objectMapper.readValue(jsonData, Invoice[].class);

    // resolve customer references, reset versions
    for (Invoice invoice : invoices) {
      invoice.setCustomer(customerRepository.findAll().get(invoice.getVersion()));
      invoice.setVersion(null);
    }
    return Arrays.asList(invoices);
  }
}