package com.booking.property.service;

import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PropertyService {
    PropertyDto createProperty(PropertyCreationDto dto);

    PropertyDto getPropertyById(UUID id);

    PropertyDto updateProperty(UUID id, PropertyPatchDto dto);

    Page<PropertyDto> getAll(Pageable pageable);

    List<PropertyDto> getPropertiesByOwner(UUID id);

    List<PropertyDto> getAllPropertiesByIds(Collection<UUID> ids);

    PropertyDto changeActivePropertyState(UUID id, Boolean state);
}
