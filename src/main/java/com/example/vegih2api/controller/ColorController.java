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

import com.example.vegih2api.model.Color;
import com.example.vegih2api.service.ColorService;

@RestController
@RequestMapping("/color")
public class ColorController {

    @Autowired
    ColorService colorService;

    @GetMapping("/list")
    public ResponseEntity<List<Color>> getAll(@RequestParam(required = false) String nameJa,
            @RequestParam(required = false) String nameEn) {
        if (nameJa != null && nameEn != null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        try {
            List<Color> colorList = this.colorService.getAll(nameJa, nameEn);

            if (colorList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(colorList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Color> getById(@PathVariable("id") long id) {
        Color color = this.colorService.getById(id);

        if (color != null)
            return new ResponseEntity<>(color, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/")
    public ResponseEntity<Color> create(@RequestBody Color color) {
        try {
            Color resultColor = this.colorService.create(color);
            return new ResponseEntity<>(resultColor, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Color> update(@PathVariable("id") long id, @RequestBody Color color) {
        try {
            Color resultColor = this.colorService.update(id, color);
            return new ResponseEntity<>(resultColor, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> delete(@PathVariable("id") long id) {
        try {
            this.colorService.deletedById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<HttpStatus> deleteAll() {
        try {
            this.colorService.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
