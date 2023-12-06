package com.sgruendel.nextjs_dashboard.migration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgruendel.nextjs_dashboard.domain.Revenue;
import com.sgruendel.nextjs_dashboard.repos.RevenueRepository;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "revenue-initializer", order = "4", author = "sgruendel")
public class RevenueInitializerChangeUnit {

  private final MongoTemplate mongoTemplate;

  private final RevenueRepository revenueRepository;

  public RevenueInitializerChangeUnit(MongoTemplate mongoTemplate, RevenueRepository revenueRepository) {
    this.mongoTemplate = mongoTemplate;
    this.revenueRepository = revenueRepository;
  }

  @BeforeExecution
  public void beforeExecution() {
    mongoTemplate.createCollection(Revenue.COLLECTION_NAME);
    // TODO ? .createIndex(new Document("month", 1), new
    // IndexOptions().name("month").unique(true));

  }

  @RollbackBeforeExecution
  public void rollbackBeforeExecution() {
    mongoTemplate.dropCollection(Revenue.COLLECTION_NAME);
  }

  @Execution
  public void execution() throws IOException {

    revenueRepository.saveAll(getRevenues());
  }

  @RollbackExecution
  public void rollbackExecution() {

    revenueRepository.deleteAll();
  }

  private static List<Revenue> getRevenues() throws IOException {
    final String jsonData = """
            [
              { "month": "Jan", "revenue": 2000 },
              { "month": "Feb", "revenue": 1800 },
              { "month": "Mar", "revenue": 2200 },
              { "month": "Apr", "revenue": 2500 },
              { "month": "May", "revenue": 2300 },
              { "month": "Jun", "revenue": 3200 },
              { "month": "Jul", "revenue": 3500 },
              { "month": "Aug", "revenue": 3700 },
              { "month": "Sep", "revenue": 2500 },
              { "month": "Oct", "revenue": 2800 },
              { "month": "Nov", "revenue": 3000 },
              { "month": "Dec", "revenue": 4800 }
            ]
        """;
    final ObjectMapper objectMapper = new ObjectMapper();
    // Deserialize JSON array to a list of Revenue objects
    return Arrays.asList(objectMapper.readValue(jsonData, Revenue[].class));
  }

}