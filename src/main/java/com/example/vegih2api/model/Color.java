package com.example.vegih2api.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;

@Entity
@Data
@Table(name = "colors")
public class Color {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "name_ja", nullable = false, unique = true)
	private String nameJa;

	@Column(name = "name_en", nullable = false, unique = true)
	private String nameEn;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@OneToMany(
		mappedBy = "color", 
		fetch = FetchType.LAZY, 
		cascade = CascadeType.ALL, 
		orphanRemoval = true)
	@JsonBackReference
	private List<Vegitable> vegitableList;
}
