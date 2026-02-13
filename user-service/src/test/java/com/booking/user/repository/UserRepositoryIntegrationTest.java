package com.booking.user.repository;

import com.booking.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("users")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Autowired
    private UserRepository userRepository;

    @Test
    void saveNewUser() {
        var user = getUser();
        userRepository.save(user);

        var savedUser = userRepository.findByEmail(user.getEmail());

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get()).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    void findUserById() {
        var user = getUser();
        var foundUser = userRepository.save(user);

        assertTrue(userRepository.findByIdAndIsDeletedFalse(foundUser.getId()).isPresent());
        assertThat(user.getEmail()).isEqualTo(foundUser.getEmail());
        assertThat(user.getId()).isEqualTo(foundUser.getId());
    }

    @Test
    void findUserByEmail() {
        var user = getUser();
        userRepository.save(user);

        var found = userRepository.findByEmail(user.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findUsersByIdAndDeletedFalseAndTrue() {
        var user1 = new User("uName", "uLastname", "email@test.ru", false);
        var user2 = new User("uName1", "uLastname1", "email1@test.ru", false);
        var user3 = new User("uName2", "uLastname2", "email2@test.ru", true);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        var result = userRepository.findByIdInAndIsDeletedFalse(List.of(user1.getId(), user2.getId(), user3.getId()));

        assertThat(result.size()).isEqualTo(2);
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        assertFalse(result.contains(user3));
    }

    @Test
    void findUsersByIdIds() {
        var user1 = new User("uName", "uLastname", "email@test.ru", false);
        var user2 = new User("uName1", "uLastname1", "email1@test.ru", false);
        var user3 = new User("uName2", "uLastname2", "email2@test.ru", true);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        var result = userRepository.findByIdIn(List.of(user1.getId(), user2.getId(), user3.getId()));

        assertThat(result.size()).isEqualTo(3);
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        assertTrue(result.contains(user3));
    }


    private User getUser() {
        return new User("TestName", "TestLastName", getRandomEmail(), false);
    }

    private String getRandomEmail() {
        return  "user_" + UUID.randomUUID() + "@test.com";
    }
}
