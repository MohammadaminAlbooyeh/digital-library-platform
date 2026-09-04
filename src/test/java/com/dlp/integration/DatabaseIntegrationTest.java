package com.dlp.integration;

import com.dlp.model.entity.User;
import com.dlp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@EntityScan(basePackages = "com.dlp.model.entity")
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
class DatabaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("dlp_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.test.database.replace", () -> "none");
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name",
                () -> com.mysql.cj.jdbc.Driver.class.getName());
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void repositoryWorksThroughFlywayMigrations() {
        User user = new User();
        user.setName("Integration Test");
        user.setEmail("integration@test.com");
        user.setPassword("hashed");
        user.setRole("USER");

        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        var found = userRepository.findByEmail("integration@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Integration Test");

        assertThat(userRepository.existsByEmail("integration@test.com")).isTrue();
    }
}
