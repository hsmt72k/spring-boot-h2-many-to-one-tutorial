package com.example.vegih2api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vegih2api.model.Vegitable;

public interface VegitableRepository extends JpaRepository<Vegitable, Long> {
    List<Vegitable> findByColor(String color);
    List<Vegitable> findByNameContaining(String name);
}
