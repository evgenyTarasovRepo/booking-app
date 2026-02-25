package com.booking.property.controller;

import com.booking.property.dto.PropertyCreationDto;
import com.booking.property.dto.PropertyDto;
import com.booking.property.dto.PropertyPatchDto;
import com.booking.property.service.PropertyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@AllArgsConstructor
@Validated
@Slf4j
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyDto> createProperty(@RequestBody @Valid PropertyCreationDto creationDto) {
        log.info("Creating property '{}' for owner {}", creationDto.name(), creationDto.ownerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(creationDto));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDto> getProperty(@PathVariable("propertyId") UUID propertyId) {
        log.debug("Getting property {}", propertyId);
        return ResponseEntity.ok(propertyService.getPropertyById(propertyId));
    }

    @PatchMapping("/{propertyId}")
    public ResponseEntity<PropertyDto> updateProperty(
            @PathVariable("propertyId") UUID propertyId,
            @RequestBody @Valid PropertyPatchDto patchDto) {
        log.info("Updating property {}", propertyId);
        return ResponseEntity.ok(propertyService.updateProperty(propertyId, patchDto));
    }

    @GetMapping
    public ResponseEntity<Page<PropertyDto>> getProperties(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.debug("Getting properties page={}, size={}", page, size);
        return ResponseEntity.ok(propertyService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PropertyDto>> getPropertiesByOwnerId(@PathVariable("ownerId") UUID ownerId) {
        log.debug("Getting properties for owner {}", ownerId);
        return ResponseEntity.ok(propertyService.getPropertiesByOwnerId(ownerId));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<PropertyDto>> getAllPropertiesByIds(
            @RequestBody
            @Size(min = 1, max = 100, message = "Batch size must be between 1 and 100") Set<UUID> ids) {
        log.debug("Getting properties by ids, count={}", ids.size());
        return ResponseEntity.ok(propertyService.getAllPropertiesByIds(ids));
    }

    @PatchMapping("/{propertyId}/status")
    public ResponseEntity<PropertyDto> changePropertyState(
            @PathVariable("propertyId") UUID propertyId,
            @RequestParam("active") Boolean state) {
        log.info("Change state for property {}", propertyId);
        return ResponseEntity.ok(propertyService.changeActivePropertyState(propertyId, state));
    }
}
