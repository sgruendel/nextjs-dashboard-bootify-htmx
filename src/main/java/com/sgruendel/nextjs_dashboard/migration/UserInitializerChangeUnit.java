package com.sgruendel.nextjs_dashboard.migration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.IndexOptions;
import com.sgruendel.nextjs_dashboard.domain.User;
import com.sgruendel.nextjs_dashboard.repos.UserRepository;

import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackBeforeExecution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "user-initializer", order = "1", author = "sgruendel")
public class UserInitializerChangeUnit {

  private final MongoTemplate mongoTemplate;

  private final UserRepository userRepository;

  public UserInitializerChangeUnit(MongoTemplate mongoTemplate, UserRepository userRepository) {
    this.mongoTemplate = mongoTemplate;
    this.userRepository = userRepository;
  }

  @BeforeExecution
  public void beforeExecution() {
    mongoTemplate.createCollection(User.COLLECTION_NAME)
        .createIndex(new Document("email", 1), new IndexOptions().name("email").unique(true));

  }

  @RollbackBeforeExecution
  public void rollbackBeforeExecution() {
    mongoTemplate.dropCollection(User.COLLECTION_NAME);
  }

  @Execution
  public void execution() throws IOException {

    userRepository.saveAll(getUsers());
  }

  @RollbackExecution
  public void rollbackExecution() {

    userRepository.deleteAll();
  }

  private static List<User> getUsers() throws IOException {
    final String jsonData = """
        [
            {
              "name": "User",
              "email": "user@nextmail.com",
              "password": "123456"
            }
          ]
                      """;
    final ObjectMapper objectMapper = new ObjectMapper();
    // Deserialize JSON array to a list of User objects
    User[] users = objectMapper.readValue(jsonData, User[].class);

    // Hash passwords
    for (User user : users) {
      user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
    }

    return Arrays.asList(users);

  }
}