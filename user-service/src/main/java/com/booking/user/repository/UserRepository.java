package com.booking.user.repository;

import com.booking.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndIsDeletedFalse(UUID userId);

    List<User> findByIdInAndIsDeletedFalse(Collection<UUID> id);

    Optional<User> findByEmail(String email);

    List<User> findByIdIn(Collection<UUID> id);
}
