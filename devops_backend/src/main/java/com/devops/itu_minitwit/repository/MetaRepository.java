package com.devops.itu_minitwit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devops.itu_minitwit.domain.Meta;

public interface MetaRepository extends JpaRepository<Meta, String> {
}

