package com.booking.property.repository;

import com.booking.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    List<Property> findAllByOwnerId(UUID userId);

    List<Property> findAllByIdIn(Collection<UUID> ids);
}
