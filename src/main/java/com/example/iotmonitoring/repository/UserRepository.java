package com.example.iotmonitoring.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.iotmonitoring.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
