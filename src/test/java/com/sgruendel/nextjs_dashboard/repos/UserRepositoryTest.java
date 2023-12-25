package com.sgruendel.nextjs_dashboard.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.sgruendel.nextjs_dashboard.NextjsDashboardApplication;
import com.sgruendel.nextjs_dashboard.domain.User;

@DataMongoTest
@ContextConfiguration(classes = NextjsDashboardApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private List<User> users;

    @BeforeEach
    public void setupTestData() {
        final User user1 = new User();
        user1.setName("username");
        user1.setEmail("User@Email.Org");
        user1.setPassword("hashed password");
        users = List.of(user1);
    }

    @Test
    void testEmptyRepository() {
        assertEquals(0, userRepository.count());
        assertEquals(0, userRepository.findAll().size());
    }

    @Test
    void testInitializedRepository() {
        users.forEach(userRepository::save);
        assertEquals(users.size(), userRepository.count());
        assertEquals(users.size(), userRepository.findAll().size());
    }

    @Test
    void testExistsById() {
        users.forEach(userRepository::save);
        final User user1 = users.get(0);

        assertFalse(userRepository.existsById("unknown id"));
        assertTrue(userRepository.existsById(user1.getId()));
    }

    @Test
    void testExistsByEmailIgnoreCase() {
        users.forEach(userRepository::save);
        final User user1 = users.get(0);

        assertFalse(userRepository.existsByEmailIgnoreCase("unknown email"));
        assertTrue(userRepository.existsByEmailIgnoreCase(user1.getEmail()));
        assertTrue(userRepository.existsByEmailIgnoreCase(user1.getEmail().toUpperCase()));
        assertTrue(userRepository.existsByEmailIgnoreCase(user1.getEmail().toLowerCase()));
    }

    @Test
    void testFindByEmailIgnoreCase() {
        users.forEach(userRepository::save);
        final User user1 = users.get(0);

        assertTrue(userRepository.findByEmailIgnoreCase("unknown email").isEmpty());
        assertTrue(userRepository.findByEmailIgnoreCase(user1.getEmail()).isPresent());
        assertTrue(userRepository.findByEmailIgnoreCase(user1.getEmail().toUpperCase()).isPresent());
        assertTrue(userRepository.findByEmailIgnoreCase(user1.getEmail().toLowerCase()).isPresent());
    }

    @AfterEach
    void tearDown() {
        // no transactions with local MongoDB so we must clean up after each test
        userRepository.deleteAll();
    }

}
