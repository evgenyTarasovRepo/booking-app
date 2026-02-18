package com.booking.property.mapper;

import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.entity.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    PropertyDto toPropertyDto(Property property);

    List<PropertyDto> toPropertyDtoList(List<Property> properties);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Property toPropertyEntity(PropertyCreationDto propertyCreationDto);
}
