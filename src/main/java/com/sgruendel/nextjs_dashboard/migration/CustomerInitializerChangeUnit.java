package com.sgruendel.nextjs_dashboard.migration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.repos.CustomerRepository;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "customer-initializer", order = "2", author = "sgruendel")
public class CustomerInitializerChangeUnit {

        private final MongoTemplate mongoTemplate;

        private final CustomerRepository customerRepository;

        public CustomerInitializerChangeUnit(MongoTemplate mongoTemplate, CustomerRepository customerRepository) {
                this.mongoTemplate = mongoTemplate;
                this.customerRepository = customerRepository;
        }

        @BeforeExecution
        public void beforeExecution() {
                mongoTemplate.createCollection(Customer.COLLECTION_NAME)
                                .createIndexes(List.of(
                                                new IndexModel(new Document("name", 1),
                                                                new IndexOptions().name("name")),
                                                new IndexModel(new Document("email", 1),
                                                                new IndexOptions().name("email").unique(true))));
        }

        @RollbackBeforeExecution
        public void rollbackBeforeExecution() {
                mongoTemplate.dropCollection(Customer.COLLECTION_NAME);
        }

        @Execution
        public void execution() throws IOException {

                customerRepository.saveAll(getCustomers());
        }

        @RollbackExecution
        public void rollbackExecution() {

                customerRepository.deleteAll();
        }

        private static List<Customer> getCustomers() throws IOException {
                final String jsonData = """
                                [
                                  {
                                  "name": "Delba de Oliveira",
                                  "email": "delba@oliveira.com",
                                  "imageUrl": "/customers/delba-de-oliveira.png"
                                  },
                                  {
                                  "name": "Lee Robinson",
                                  "email": "lee@robinson.com",
                                  "imageUrl": "/customers/lee-robinson.png"
                                  },
                                  {
                                  "name": "Hector Simpson",
                                  "email": "hector@simpson.com",
                                  "imageUrl": "/customers/hector-simpson.png"
                                  },
                                  {
                                  "name": "Steven Tey",
                                  "email": "steven@tey.com",
                                  "imageUrl": "/customers/steven-tey.png"
                                  },
                                  {
                                  "name": "Steph Dietz",
                                  "email": "steph@dietz.com",
                                  "imageUrl": "/customers/steph-dietz.png"
                                  },
                                  {
                                  "name": "Michael Novotny",
                                  "email": "michael@novotny.com",
                                  "imageUrl": "/customers/michael-novotny.png"
                                  },
                                  {
                                  "name": "Evil Rabbit",
                                  "email": "evil@rabbit.com",
                                  "imageUrl": "/customers/evil-rabbit.png"
                                  },
                                  {
                                  "name": "Emil Kowalski",
                                  "email": "emil@kowalski.com",
                                  "imageUrl": "/customers/emil-kowalski.png"
                                  },
                                  {
                                  "name": "Amy Burns",
                                  "email": "amy@burns.com",
                                  "imageUrl": "/customers/amy-burns.png"
                                  },
                                  {
                                  "name": "Balazs Orban",
                                  "email": "balazs@orban.com",
                                  "imageUrl": "/customers/balazs-orban.png"
                                  }
                                ]
                                  """;
                final ObjectMapper objectMapper = new ObjectMapper();
                // Deserialize JSON array to a list of Customer objects
                return Arrays.asList(objectMapper.readValue(jsonData, Customer[].class));

        }
}