package com.example.vegih2api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vegih2api.model.Vegitable;
import com.example.vegih2api.repository.VegitableRepository;

@Service
public class VegitableService {

    @Autowired
    VegitableRepository vegitableRepository;

    public List<Vegitable> getAll(String name) {
        List<Vegitable> vegitableList = new ArrayList<Vegitable>();
        
        if (name == null)
                this.vegitableRepository.findAll().forEach(vegitableList::add);
            else
                this.vegitableRepository.findByNameContaining(name).forEach(vegitableList::add);
        return vegitableList;
    }

    public Vegitable getById(long id) {
        Optional<Vegitable> vegitableData = this.vegitableRepository.findById(id);

        if (vegitableData.isPresent()) return vegitableData.get();
        return null;
    }

    public Vegitable create(Vegitable vegitable) {
        LocalDateTime now = LocalDateTime.now();

        vegitable.setCreatedAt(now);
        vegitable.setUpdatedAt(now);
        return this.vegitableRepository.save(vegitable);
    }

    public Vegitable update(long id, Vegitable vegitable) throws Exception {
        Optional<Vegitable> vegitableData = this.vegitableRepository.findById(id);

        if (vegitableData.isPresent()) {
            Vegitable resultVegitable = vegitableData.get();
            resultVegitable.setName(vegitable.getName());
            resultVegitable.setColor(vegitable.getColor());
            resultVegitable.setPrice(vegitable.getPrice());

            LocalDateTime now = LocalDateTime.now();
            resultVegitable.setUpdatedAt(now);
            return this.vegitableRepository.save(resultVegitable);
        } else {
            throw new Exception();
        }
    }

    public void deletedById(long id) throws Exception {
        Optional<Vegitable> vegitableData = this.vegitableRepository.findById(id);

        if (vegitableData.isPresent()) {
            this.vegitableRepository.deleteById(id);
            return;
        }
        throw new Exception();
        
    }

    public void deleteAll() throws Exception {
        this.vegitableRepository.deleteAll();
    }

    public List<Vegitable> findByColor(String color) {
        return this.vegitableRepository.findByColor(color);
    }
}
