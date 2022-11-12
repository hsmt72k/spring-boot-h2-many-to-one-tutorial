package com.example.vegih2api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vegih2api.model.Color;

public interface ColorRepository extends JpaRepository<Color, Long> {
    List<Color> findByNameJaContaining(String nameJa);
    List<Color> findByNameEnContaining(String nameEn);
}
