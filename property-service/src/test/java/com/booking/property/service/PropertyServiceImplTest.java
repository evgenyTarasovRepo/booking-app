package com.booking.property.service;

import com.booking.property.client.UserServiceClient;
import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import com.booking.property.dto.UserDto;
import com.booking.property.entity.Property;
import com.booking.property.entity.PropertyType;
import com.booking.property.exception.OwnerNotFoundException;
import com.booking.property.exception.PropertyNotFoundException;
import com.booking.property.exception.UserServiceUnavailableException;
import com.booking.property.mapper.PropertyMapper;
import com.booking.property.repository.PropertyRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PropertyMapper propertyMapper;

    PropertyServiceImpl propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyServiceImpl(propertyRepository, propertyMapper, userServiceClient);
    }

    private final UUID userId = UUID.randomUUID();
    private final UUID propertyId = UUID.randomUUID();

    PropertyCreationDto propertyCreationDto = new PropertyCreationDto("TestProperty", "TestDescription", "TestAddress", "TestCity", "TestCountry",
            PropertyType.APARTMENT, new BigDecimal("50.00"), 3,  userId
    );


    @Test
    void shouldCreateProperty() {
        var property = createProperty();
        property.setId(propertyId);
        var propertyDto = createPropertyDto(propertyId, property.getOwnerId(), property.getCreatedAt(), property.getIsActive());

        when(userServiceClient.getUserById(userId)).thenReturn(new UserDto(userId, "Name", "Lastname", "email@test.com", LocalDateTime.now(), false));
        when(propertyMapper.toPropertyEntity(propertyCreationDto)).thenReturn(property);
        when(propertyRepository.saveAndFlush(property)).thenReturn(property);
        when(propertyMapper.toPropertyDto(property)).thenReturn(propertyDto);

        var result = propertyService.createProperty(propertyCreationDto);

        assertThat(result).usingRecursiveComparison().isEqualTo(propertyDto);
    }

    @Test
    void shouldThrowOwnerNotFoundException_WhenUserNotFound() {

        when(userServiceClient.getUserById(userId)).thenReturn(new UserDto(userId, "Name", "Lastname", "email@test.com", LocalDateTime.now(), true));

        assertThatThrownBy(() -> propertyService.createProperty(propertyCreationDto))
                    .isInstanceOf(OwnerNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldThrowFeignNotFoundException_WhenUserNotFound() {

        when(userServiceClient.getUserById(userId)).thenThrow(FeignException.NotFound.class);

        assertThatThrownBy(() -> propertyService.createProperty(propertyCreationDto))
                    .isInstanceOf(OwnerNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldThrowFeignException_WhenUserServiceIsUnavailable() {

        when(userServiceClient.getUserById(userId)).thenThrow(FeignException.class);

        assertThatThrownBy(() -> propertyService.createProperty(propertyCreationDto))
                    .isInstanceOf(UserServiceUnavailableException.class)
                .hasMessageContaining("user-service is unavailable");
    }

    @Test
    void shouldReturnPropertyById() {
        var property = createProperty();
        property.setId(propertyId);
        var propertyDto = createPropertyDto(propertyId, property.getOwnerId(), property.getCreatedAt(), property.getIsActive());

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyMapper.toPropertyDto(property)).thenReturn(propertyDto);

        var result = propertyService.getPropertyById(propertyId);

        assertThat(result).usingRecursiveComparison().isEqualTo(propertyDto);
    }

    @Test
    void shouldUpdateProperty() {
        var property = createProperty();
        property.setId(propertyId);
        var patchDto = new PropertyPatchDto("UpdatedName", "UpdatedDescription", "UpdatedAdress",
                null, null, null, null, null);
        var propertyDto = new PropertyDto(propertyId, patchDto.name(), patchDto.description(), patchDto.address(), property.getCity(),
                property.getCountry(), property.getPropertyType(), property.getPricePerNight(), property.getMaxGuests(), property.getOwnerId(),
                property.getIsActive(), property.getCreatedAt());

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        doNothing().when(propertyMapper).updatePropertyEntity(patchDto, property);
        when(propertyRepository.saveAndFlush(property)).thenReturn(property);
        when(propertyMapper.toPropertyDto(property)).thenReturn(propertyDto);

        var result = propertyService.updateProperty(propertyId, patchDto);

        assertThat(result.name()).isEqualTo(patchDto.name());
        assertThat(result.description()).isEqualTo(patchDto.description());
        assertThat(result.address()).isEqualTo(patchDto.address());
        assertThat(result.city()).isEqualTo(property.getCity());
        assertThat(result.country()).isEqualTo(property.getCountry());
    }

    @Test
    void shouldThrowPropertyNotFoundExceptionWhenUpdating_WhenPropertyNotFound () {
        var propertyId = UUID.randomUUID();
        var patchDto = new PropertyPatchDto("UpdatedName", "UpdatedDescription", "UpdatedAdress",
                null, null, null, null, null);

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propertyService.updateProperty(propertyId, patchDto))
                .isInstanceOf(PropertyNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldReturnAllProperties() {
        Pageable pageable = PageRequest.of(0, 10);
        var property1 = createProperty();
        property1.setId(propertyId);
        var property2 = createProperty();
        property2.setId(UUID.randomUUID());
        var property3 = createProperty();
        property3.setId(UUID.randomUUID());

        var propertyDto1 = createPropertyDto(propertyId, property1.getOwnerId(), property1.getCreatedAt(), property1.getIsActive());
        var propertyDto2 = createPropertyDto(property2.getId(), property2.getOwnerId(), property2.getCreatedAt(), property2.getIsActive());
        var propertyDto3 = createPropertyDto(property3.getId(), UUID.randomUUID(), property3.getCreatedAt(), property3.getIsActive());

        List<Property> properties = List.of(property1, property2, property3);
        List<PropertyDto> propertyDtos = List.of(propertyDto1, propertyDto2, propertyDto3);

        Page<Property> propertyPage = new PageImpl<>(properties, pageable, propertyDtos.size());

        when(propertyRepository.findAll(pageable)).thenReturn(propertyPage);

        when(propertyMapper.toPropertyDto(properties.get(0))).thenReturn(propertyDtos.get(0));
        when(propertyMapper.toPropertyDto(properties.get(1))).thenReturn(propertyDtos.get(1));
        when(propertyMapper.toPropertyDto(properties.get(2))).thenReturn(propertyDtos.get(2));

        var result = propertyService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void getAllPropertiesByIds() {
        var property1 = createProperty();
        property1.setId(propertyId);
        var property2 = createProperty();
        property2.setId(UUID.randomUUID());
        var property3 = createProperty();
        property3.setId(UUID.randomUUID());

        var ids = List.of(property1.getId(), property2.getId(), property3.getId());

        var propertyDto1 = createPropertyDto(propertyId, property1.getOwnerId(), property1.getCreatedAt(), property1.getIsActive());
        var propertyDto2 = createPropertyDto(property2.getId(), property2.getOwnerId(), property2.getCreatedAt(), property2.getIsActive());
        var propertyDto3 = createPropertyDto(property3.getId(), UUID.randomUUID(), property3.getCreatedAt(), property3.getIsActive());

        var dtoList = List.of(propertyDto1, propertyDto2, propertyDto3);

        when(propertyRepository.findAllByIdIn(ids)).thenReturn(List.of(property1, property2, property3));
        when(propertyMapper.toPropertyDtoList(List.of(property1, property2, property3))).thenReturn(List.of(propertyDto1, propertyDto2, propertyDto3));

        var result = propertyService.getAllPropertiesByIds(ids);

        assertThat(result)
                .hasSize(dtoList.size())
                .usingRecursiveComparison()
                .isEqualTo(dtoList);
    }

    @Test
    void getAllPropertiesByOwnerId() {
        var property1 = createProperty();
        property1.setId(propertyId);
        var property2 = createProperty();
        property2.setId(UUID.randomUUID());
        var property3 = createProperty();
        property3.setId(UUID.randomUUID());

        var propertyDto1 = createPropertyDto(propertyId, property1.getOwnerId(), property1.getCreatedAt(), property1.getIsActive());
        var propertyDto2 = createPropertyDto(property2.getId(), property1.getOwnerId(), property2.getCreatedAt(), property2.getIsActive());
        var propertyDto3 = createPropertyDto(property3.getId(), property1.getOwnerId(), property3.getCreatedAt(), property3.getIsActive());

        var dtoList = List.of(propertyDto1, propertyDto2, propertyDto3);

        when(propertyRepository.findAllByOwnerId(property1.getOwnerId())).thenReturn(List.of(property1, property2, property3));
        when(propertyMapper.toPropertyDtoList(List.of(property1, property2, property3))).thenReturn(dtoList);

        var result = propertyService.getPropertiesByOwnerId(property1.getOwnerId());

        assertThat(result)
                .hasSize(dtoList.size())
                .usingRecursiveComparison()
                .isEqualTo(dtoList);
    }

    @Test
    void shouldChangeActivePropertyState() {
        var property = createProperty();
        property.setId(propertyId);
        var propertyDto = createPropertyDto(propertyId, property.getOwnerId(), property.getCreatedAt(), false);

        assertTrue(property.getIsActive());

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(propertyRepository.saveAndFlush(property)).thenReturn(property);
        when(propertyMapper.toPropertyDto(property)).thenReturn(propertyDto);

        var result = propertyService.changeActivePropertyState(propertyId, false);

        assertThat(result).usingRecursiveComparison().isEqualTo(propertyDto);
        assertFalse(result.isActive());
    }

    private Property createProperty() {
        return new Property("TestProperty", "TestDescription", "TestAddress", "TestCity", "TestCountry",
                PropertyType.APARTMENT, new BigDecimal("50.00"), 3, UUID.randomUUID(), true);
    }

    private PropertyDto createPropertyDto(UUID propertyId, UUID ownerId, LocalDateTime createdAt, Boolean active) {
        return new PropertyDto(propertyId, "TestProperty", "TestDescription", "TestAddress", "TestCity", "TestCountry",
                PropertyType.APARTMENT, new BigDecimal("50.00"), 3, ownerId, active, createdAt);
    }
}
