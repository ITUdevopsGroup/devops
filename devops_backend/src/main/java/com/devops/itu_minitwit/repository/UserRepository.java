package com.devops.itu_minitwit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devops.itu_minitwit.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);
}

