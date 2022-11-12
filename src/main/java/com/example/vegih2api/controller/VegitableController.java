package com.example.vegih2api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.vegih2api.model.Vegitable;
import com.example.vegih2api.service.VegitableService;

@RestController
@RequestMapping("/vegitable")
public class VegitableController {

    @Autowired
    VegitableService vegitableService;

    @GetMapping("/list")
    public ResponseEntity<List<Vegitable>> getAll(@RequestParam(required = false) String name) {
        try {
            List<Vegitable> vegitableList = this.vegitableService.getAll(name);

            if (vegitableList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(vegitableList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vegitable> getById(@PathVariable("id") long id) {
        Vegitable vegitable = this.vegitableService.getById(id);

        if (vegitable != null) return new ResponseEntity<>(vegitable, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/")
    public ResponseEntity<Vegitable> create(@RequestBody Vegitable vegitable) {
        try {
            Vegitable resultVegitable = this.vegitableService.create(vegitable);
            return new ResponseEntity<>(resultVegitable, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vegitable> update(@PathVariable("id") long id, @RequestBody Vegitable vegitable) {
        try {
            Vegitable resultVegitable = this.vegitableService.update(id, vegitable);
            return new ResponseEntity<>(resultVegitable, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") long id) {
        try {
            this.vegitableService.deletedById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<HttpStatus> deleteAll() {
        try {
            this.vegitableService.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/")
    public ResponseEntity<List<Vegitable>> findByColor(@RequestParam(required = false) String color) {
        try {
            List<Vegitable> vegitableList = this.vegitableService.findByColor(color);
            
            if (vegitableList.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            return new ResponseEntity<>(vegitableList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
