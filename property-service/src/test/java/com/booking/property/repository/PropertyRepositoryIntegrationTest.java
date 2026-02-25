package com.booking.property.repository;

import com.booking.property.entity.Property;
import com.booking.property.entity.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PropertyRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("properties")
            .withUsername("property")
            .withPassword("password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        propertyRepository.deleteAll();
    }

    @Autowired
    private PropertyRepository propertyRepository;

    @Test
    void saveProperty() {
        var property = createProperty();

        var saved = propertyRepository.saveAndFlush(property);

        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().isEqualTo(property);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getName()).isEqualTo(property.getName());
        assertThat(saved.getOwnerId()).isEqualTo(property.getOwnerId());
    }

    @Test
    void findPropertyById() {
        var property = createProperty();
        var saved = propertyRepository.saveAndFlush(property);

        Optional<Property> found = propertyRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found).isNotNull();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getOwnerId()).isEqualTo(property.getOwnerId());
    }

    @Test
    void findPropertyById_shouldReturnEmpty() {
        var notExistentId = UUID.randomUUID();

        Optional<Property> found = propertyRepository.findById(notExistentId);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_WithPagination_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            propertyRepository.save(createProperty());
        }

        Page<Property> page = propertyRepository.findAll(PageRequest.of(1, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    void findPropertiesByOwnerId() {
        var ownerId = UUID.randomUUID();
        var otherOwnerId = UUID.randomUUID();

        var property1 = createProperty();
        property1.setOwnerId(ownerId);

        var property2 = createProperty();
        property2.setOwnerId(ownerId);

        var property3 = createProperty();
        property3.setOwnerId(otherOwnerId);

        propertyRepository.saveAll(List.of(property1, property2, property3));

        var result = propertyRepository.findAllByOwnerId(ownerId);

        assertThat(result)
                .hasSize(2)
                .extracting(Property::getOwnerId)
                .containsOnly(ownerId);

        assertThat(result)
                .extracting(Property::getId)
                .containsExactlyInAnyOrder(property1.getId(), property2.getId());
    }

    @Test
    void findPropertiesByIds() {
        var property1 = createProperty();
        var property2 = createProperty();
        var property3 = createProperty();

        propertyRepository.saveAll(List.of(property1, property2, property3));

        var ids = List.of(property1.getId(), property2.getId());

        var result = propertyRepository.findAllByIdIn(ids);

        assertThat(result)
                .hasSize(2)
                .extracting(Property::getId)
                .containsExactlyInAnyOrder(property1.getId(), property2.getId())
                .doesNotContain(property3.getId());
    }

    @Test
    void findAllByIdIn_WhenSomeIdsNotExist_ShouldReturnOnlyExisting() {
        var existing = propertyRepository.save(createProperty());
        var nonExistentId = UUID.randomUUID();

        var result = propertyRepository.findAllByIdIn(
                List.of(existing.getId(), nonExistentId)
        );

        assertThat(result)
                .hasSize(1)
                .extracting(Property::getId)
                .containsOnly(existing.getId());
    }

    @Test
    void findAllByIdIn_WhenEmptyList_ShouldReturnEmptyList() {
        propertyRepository.save(createProperty());

        List<Property> result = propertyRepository.findAllByIdIn(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void changePropertyActiveState() {
        var property = createProperty();
        propertyRepository.saveAndFlush(property);

        property.setIsActive(false);

        var saved = propertyRepository.saveAndFlush(property);

        propertyRepository.flush();

        var reloaded = propertyRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getIsActive()).isFalse();
    }

    private Property createProperty() {
        return new Property("TestProperty", "TestDescription", "TestAdress", "TestCity", "TestCountry",
                PropertyType.APARTMENT, new BigDecimal("50.00"), 3, UUID.randomUUID(), true);
    }
}
