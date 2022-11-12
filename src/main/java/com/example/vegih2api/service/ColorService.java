package com.example.vegih2api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.vegih2api.model.Color;
import com.example.vegih2api.repository.ColorRepository;

@Service
public class ColorService {

    @Autowired
    ColorRepository colorRepository;

    public List<Color> getAll(String nameJa, String nameEn) {
        List<Color> colorList = new ArrayList<Color>();

        if (nameJa == null && nameEn == null)
            this.colorRepository.findAll().forEach(colorList::add);
        else if (nameJa != null)
            this.colorRepository.findByNameJaContaining(nameJa).forEach(colorList::add);
        else
            this.colorRepository.findByNameEnContaining(nameEn).forEach(colorList::add);
        return colorList;
    }

    public Color getById(long id) {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent())
            return colorData.get();
        return null;
    }

    public Color create(Color color) {
        LocalDateTime now = LocalDateTime.now();

        color.setCreatedAt(now);
        color.setUpdatedAt(now);
        return this.colorRepository.save(color);
    }

    public Color update(long id, Color color) throws Exception {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent()) {
            Color resultColor= colorData.get();
            resultColor.setNameJa(color.getNameJa());
            resultColor.setNameEn(color.getNameEn());

            LocalDateTime now = LocalDateTime.now();
            resultColor.setUpdatedAt(now);
            return this.colorRepository.save(resultColor);
        } else {
            throw new Exception();
        }
    }

    public void deletedById(long id) throws Exception {
        Optional<Color> colorData = this.colorRepository.findById(id);

        if (colorData.isPresent()) {
            this.colorRepository.deleteById(id);
            return;
        }
        throw new Exception();
        
    }

    public void deleteAll() throws Exception {
        this.colorRepository.deleteAll();
    }
}
