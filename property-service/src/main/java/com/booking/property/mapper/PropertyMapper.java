package com.booking.property.mapper;

import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import com.booking.property.entity.Property;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    PropertyDto toPropertyDto(Property property);

    List<PropertyDto> toPropertyDtoList(List<Property> properties);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Property toPropertyEntity(PropertyCreationDto propertyCreationDto);

    //PatchDto -> Property - update of existing Property
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    void updatePropertyEntity(PropertyPatchDto propertyPatchDto, @MappingTarget Property property);
}
