package com.di.marinetracker.backendspringboot.repositories;

import com.di.marinetracker.backendspringboot.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository interface for User entity
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Find a user by their unique id
    User findByid(String id);

    // Find a user by their username
    Optional<User> findByUserName(String userName);

    // Check if a user exists by username
    Boolean existsByUserName(String username);

    // Check if a user exists by email
    Boolean existsByEmail(String email);

    // Check if a user with a given id has a vessel with the given mmsi in their fleet
    Boolean existsByIdAndFleetMmsi(String userId, String mmsi);
}
