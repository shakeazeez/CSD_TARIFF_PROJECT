package com.tariff.calculation.tariffCalc.controller;

import com.tariff.calculation.tariffCalc.dto.TariffDeleteDTO;
import com.tariff.calculation.tariffCalc.service.CrudService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tariff/admin")
public class AdminController {
    private final CrudService crudService;
    
    public AdminController(CrudService crudService) {
        this.crudService = crudService;
    }
    
    @DeleteMapping("/item/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable int itemId) {
        try {
            crudService.deleteItem(itemId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @DeleteMapping("/country/{id}")
    public ResponseEntity<?> deleteCountry(@PathVariable int countryId) {
        try {
            crudService.deleteCountry(countryId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/tariff/{id}")
    public ResponseEntity<?> deleteItem(@RequestBody TariffDeleteDTO tariffDeleteDTO) {
        try {
            crudService.deleteTariff(tariffDeleteDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}