package com.booking.user.repository;

import com.booking.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndDeleteFalse(UUID userId);
    List<User> findByIdsAndDeleteFalse(Iterable<UUID> userIds);

    Optional<User> findByEmail(String email);

    List<User> findByIdIn(Iterable<UUID> userIds);
}
