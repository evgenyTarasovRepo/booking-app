package com.booking.property.service;

import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PropertyService {
    PropertyDto createProperty(PropertyCreationDto dto);

    PropertyDto getPropertyById(String id);

    PropertyDto updateProperty(UUID id, PropertyPatchDto dto);

    Page<List<PropertyDto>> getAllProperties(Pageable pageable);

    List<PropertyDto> getPropertiesByOwner(UUID id);

    PropertyDto changeActivePropertyState(UUID id, Boolean active);
}
