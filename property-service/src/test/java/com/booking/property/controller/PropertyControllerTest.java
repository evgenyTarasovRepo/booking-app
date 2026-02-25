package com.booking.property.controller;

import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import com.booking.property.entity.PropertyType;
import com.booking.property.exception.OwnerNotFoundException;
import com.booking.property.exception.PropertyNotFoundException;
import com.booking.property.exception.exceptionhandler.GlobalExceptionHandler;
import com.booking.property.service.PropertyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropertyController.class)
@Import(GlobalExceptionHandler.class)
public class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropertyService propertyService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID PROPERTY_ID = UUID.randomUUID();
    private final UUID OWNER_ID = UUID.randomUUID();

    @Test
    void createProperty_Success() throws Exception {
        var creationDto = new PropertyCreationDto("TestName", "TestDescription", "TestAddress", "TestCity", "TestCountry",
                PropertyType.BUNGALOW, new BigDecimal("50.00"), 3, OWNER_ID);
        var propertyDto = createPropertyDto(PROPERTY_ID, OWNER_ID, LocalDateTime.now(), true);

        when(propertyService.createProperty(creationDto)).thenReturn(propertyDto);

        mockMvc.perform(post("/api/v1/properties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(creationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(creationDto.name()));
    }

    @Test
    void createProperty_ValidationError_BlankFirstName() throws Exception {
        var creationDto = new PropertyCreationDto("", "TestDescription", "TestAddress", "TestCity", "TestCountry",
                PropertyType.BUNGALOW, new BigDecimal("50.00"), 3, OWNER_ID);

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        verify(propertyService, never()).createProperty(creationDto);
    }

    @Test
    void createProperty_ValidationError_BlankDescription() throws Exception {
        var creationDto = new PropertyCreationDto("TestName", "", "TestAddress", "TestCity", "TestCountry",
                PropertyType.BUNGALOW, new BigDecimal("50.00"), 3, OWNER_ID);

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        verify(propertyService, never()).createProperty(creationDto);
    }

    @Test
    void getPropertyById_Success() throws Exception {
        var propertyDto = createPropertyDto(PROPERTY_ID, OWNER_ID, LocalDateTime.now(), true);

        when(propertyService.getPropertyById(PROPERTY_ID)).thenReturn(propertyDto);

        mockMvc.perform(get("/api/v1/properties/{propertyId}", PROPERTY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PROPERTY_ID.toString()))
                .andExpect(jsonPath("$.name").value(propertyDto.name()))
                .andExpect(jsonPath("$.ownerId").value(propertyDto.ownerId().toString()))
                .andExpect(jsonPath("$.isActive").value(propertyDto.isActive()));
    }

    @Test
    void getPropertyById_WhenInvalidUuid_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/properties/{propertyId}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(propertyService);
    }

    @Test
    void getPropertyById_NotFound() throws Exception {

        when(propertyService.getPropertyById(PROPERTY_ID)).thenThrow(PropertyNotFoundException.class);

        mockMvc.perform(get("/api/v1/properties/{propertyId}", PROPERTY_ID))
                .andExpect(status()
                        .isNotFound());
    }

    @Test
    void updateProperty_Success() throws Exception {
        var propertyDto = new PropertyDto(PROPERTY_ID,"UpdateName", "UpdatedDescription", "UpdatedAddress", "TestCity", "TestCountry", PropertyType.BUNGALOW,
                 new BigDecimal("50.0"), 3, OWNER_ID,  true, LocalDateTime.now());
        var patchDto = new PropertyPatchDto("UpdateName", "UpdatedDescription", "UpdatedAddress", null, null, null, null, null);

        when(propertyService.updateProperty(PROPERTY_ID, patchDto)).thenReturn(propertyDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/properties/{propertyId}", PROPERTY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(patchDto.name()))
                .andExpect(jsonPath("$.description").value(patchDto.description()))
                .andExpect(jsonPath("$.address").value(patchDto.address()));
    }

    @Test
    void updateProperty_OwnerNotFound() throws Exception {
        var patchDto = new PropertyPatchDto("UpdateName", "UpdatedDescription", "UpdatedAddress", null, null, null, null, null);

        when(propertyService.updateProperty(PROPERTY_ID, patchDto)).thenThrow(OwnerNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/properties/{propertyId}", PROPERTY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllProperties_Success() throws Exception {
        var propertyDto = createPropertyDto(PROPERTY_ID, OWNER_ID, LocalDateTime.now(), true);
        Page<PropertyDto> page = new PageImpl<>(List.of(propertyDto));

        when(propertyService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/properties")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(propertyDto.id().toString()))
                .andExpect(jsonPath("$.content[0].name").value(propertyDto.name()))
                .andExpect(jsonPath("$.content[0].ownerId").value(propertyDto.ownerId().toString()))
                .andExpect(jsonPath("$.content[0].isActive").value(propertyDto.isActive()))
                .andExpect(jsonPath("$.content[0].createdAt").value(propertyDto.createdAt().toString()));
    }

    @Test
    void getPropertiesByOwnerId_Success() throws Exception {
        var propertyDto = createPropertyDto(PROPERTY_ID, OWNER_ID, LocalDateTime.now(), true);
        var propertyDtos = List.of(propertyDto);

        when(propertyService.getPropertiesByOwnerId(OWNER_ID)).thenReturn(propertyDtos);

        mockMvc.perform(get("/api/v1/properties/owner/{ownerId}", OWNER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(propertyDto.id().toString()))
                .andExpect(jsonPath("$[0].name").value(propertyDto.name()))
                .andExpect(jsonPath("$[0].ownerId").value(propertyDto.ownerId().toString()))
                .andExpect(jsonPath("$[0].isActive").value(propertyDto.isActive()))
                .andExpect(jsonPath("$[0].createdAt").value(propertyDto.createdAt().toString()));
    }

    @Test
    void getAllPropertiesByIds_Success() throws Exception {
        var propertyDto = createPropertyDto(PROPERTY_ID, OWNER_ID, LocalDateTime.now(), true);
        var propertyDtos = List.of(propertyDto);

        when(propertyService.getAllPropertiesByIds(Set.of(PROPERTY_ID))).thenReturn(propertyDtos);

        mockMvc.perform(post("/api/v1/properties/batch" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of(PROPERTY_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(propertyDto.id().toString()))
                .andExpect(jsonPath("$[0].name").value(propertyDto.name()))
                .andExpect(jsonPath("$[0].ownerId").value(propertyDto.ownerId().toString()))
                .andExpect(jsonPath("$[0].isActive").value(propertyDto.isActive()))
                .andExpect(jsonPath("$[0].createdAt").value(propertyDto.createdAt().toString()));
    }

    @Test
    void getAllPropertiesByIds_WhenNoneFound_ShouldReturn404() throws Exception {
        when(propertyService.getAllPropertiesByIds(anyCollection()))
                .thenThrow(PropertyNotFoundException.forProperties(Set.of(PROPERTY_ID)));

        mockMvc.perform(post("/api/v1/properties/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of(PROPERTY_ID))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllPropertiesByIds_WhenEmptySet_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/properties/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(propertyService);
    }

    @Test
    void getPropertiesByOwnerId_WhenNoProperties_ShouldReturnEmptyList() throws Exception {
        when(propertyService.getPropertiesByOwnerId(OWNER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/owner/{ownerId}", OWNER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllPropertiesByIds_WhenTooManyIds_ShouldReturn400() throws Exception {

        Set<UUID> tooManyIds = IntStream.range(0, 101)
                .mapToObj(i -> UUID.randomUUID())
                .collect(Collectors.toSet());

        mockMvc.perform(post("/api/v1/properties/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooManyIds)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(propertyService);
    }

    @Test
    void getAllPropertiesByIds_WhenMultipleIds_ShouldReturnAll() throws Exception {

        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var dto1 = createPropertyDto(id1, OWNER_ID, LocalDateTime.now(), true);
        var dto2 = createPropertyDto(id2, OWNER_ID, LocalDateTime.now(), true);

        when(propertyService.getAllPropertiesByIds(anyCollection()))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(post("/api/v1/properties/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Set.of(id1, id2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void changePropertyStatus_WhenDeactivating_ShouldReturnInactiveProperty() throws Exception {
        var propertyDto = createPropertyDto(PROPERTY_ID, OWNER_ID, LocalDateTime.now(), false);

        when(propertyService.changeActivePropertyState(PROPERTY_ID, false)).thenReturn(propertyDto);

        mockMvc.perform(patch("/api/v1/properties/{propertyId}/status", PROPERTY_ID)
                    .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(propertyService).changeActivePropertyState(PROPERTY_ID, false);
    }

    @Test
    void changePropertyStatus_WhenPropertyNotFound_ShouldReturn404() throws Exception {
        when(propertyService.changeActivePropertyState(PROPERTY_ID, true))
                .thenThrow(PropertyNotFoundException.forProperty(PROPERTY_ID));

        mockMvc.perform(patch("/api/v1/properties/{propertyId}/status", PROPERTY_ID)
                        .param("active", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePropertyStatus_WhenMissingActiveParam_ShouldReturn400() throws Exception {
        mockMvc.perform(patch("/api/v1/properties/{propertyId}/status", PROPERTY_ID))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(propertyService);
    }

    private PropertyDto createPropertyDto(UUID propertyId, UUID ownerId, LocalDateTime createdAt, Boolean active) {
        return new PropertyDto(propertyId, "TestName", "TestDescription", "TestAddress", "TestCity", "TestCountry",
                PropertyType.BUNGALOW, new BigDecimal("50.0"), 3, ownerId, active, createdAt);
    }
}
