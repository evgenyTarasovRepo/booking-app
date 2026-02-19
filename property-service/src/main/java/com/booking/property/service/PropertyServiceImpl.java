package com.booking.property.service;

import com.booking.property.client.UserServiceClient;
import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import com.booking.property.entity.Property;
import com.booking.property.exception.OwnerNotFoundException;
import com.booking.property.exception.PropertyNotFoundException;
import com.booking.property.exception.UserServiceUnavailableException;
import com.booking.property.mapper.PropertyMapper;
import com.booking.property.repository.PropertyRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public PropertyDto createProperty(PropertyCreationDto dto) {
        log.info("Creating property '{}' for owner {}", dto.name(), dto.ownerId());

        validateUserExists(dto.ownerId());

        var property = propertyMapper.toPropertyEntity(dto);
        property.setIsActive(true);

        var saved = propertyRepository.saveAndFlush(property);
        log.info("Property created with id {}", saved.getId());

        return propertyMapper.toPropertyDto(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDto getPropertyById(UUID id) {
        log.debug("Getting property by id {}", id);

        var property = propertyRepository.findById(id).orElseThrow(() -> PropertyNotFoundException.forProperty(id));
        return propertyMapper.toPropertyDto(property);
    }

    @Override
    @Transactional
    public PropertyDto updateProperty(UUID id, PropertyPatchDto dto) {
        log.info("Updating property {}", id);

        var updatedProperty = updatePropertyData(id, dto);
        var savedProperty = propertyRepository.saveAndFlush(updatedProperty);

        return propertyMapper.toPropertyDto(savedProperty);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyDto> getAll(Pageable pageable) {
        var propertyPage = propertyRepository.findAll(pageable);
        return propertyPage.map(propertyMapper::toPropertyDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyDto> getPropertiesByOwner(UUID id) {
        log.debug("Getting properties for owner {}", id);

        var properties = propertyRepository.findAllByOwnerId(id);

        return propertyMapper.toPropertyDtoList(properties);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyDto> getAllPropertiesByIds(Collection<UUID> ids) {
        log.debug("Getting properties by ids: {}", ids);

        var properties = propertyRepository.findAllByIdIn(ids);

        if (properties.isEmpty()) {
            throw PropertyNotFoundException.forProperties(ids);
        }

        return propertyMapper.toPropertyDtoList(properties);
    }

    @Override
    @Transactional
    public PropertyDto changeActivePropertyState(UUID id, Boolean state) {
        log.info("Changing property {} active state to {}", id, state);

        var property = changeActiveState(id, state);
        var savedProperty = propertyRepository.saveAndFlush(property);
        return propertyMapper.toPropertyDto(savedProperty);
    }

    private Property updatePropertyData(UUID id, PropertyPatchDto updatedDto) {
        var property = propertyRepository.findById(id).orElseThrow(() -> PropertyNotFoundException.forProperty(id));

        propertyMapper.updatePropertyEntity(updatedDto, property);

        return property;
    }

    private void validateUserExists(UUID id) {
        try {
            var user = userServiceClient.getUserById(id);

            if (user.isDeleted()) {
                throw new OwnerNotFoundException(id);
            }

        } catch (FeignException.NotFound e) {
            log.warn("Owner with id {} not found", id);
            throw new OwnerNotFoundException(id);
        } catch (FeignException e) {
            log.error("Error calling user-service: {}", e.getMessage());
            throw new UserServiceUnavailableException("Failed to validate owner: user-service is unavailable", e);
        }
    }

    private Property changeActiveState(UUID id, Boolean state) {
        var property = propertyRepository.findById(id).orElseThrow(() -> PropertyNotFoundException.forProperty(id));

        property.setIsActive(state);

        log.debug("Property {} updated successfully", id);
        return property;
    }
}
