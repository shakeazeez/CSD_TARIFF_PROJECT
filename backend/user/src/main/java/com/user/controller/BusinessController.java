package com.user.controller;

import com.user.dto.BusinessInfoDTO;
import com.user.dto.ReceiveListDTO;
import com.user.service.BusinessService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class BusinessController {

    private BusinessService businessService;
    private Logger log = LoggerFactory.getLogger(BusinessController.class);

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getBusinessUserDetails(@PathVariable String username) {
        try {
            BusinessInfoDTO businessInfoDTO = businessService.getBusinessDetails(username);
            return ResponseEntity.ok(businessInfoDTO);

        } catch (IllegalAccessError e) {
            log.info(e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return ResponseEntity.status(404).build();
        }
    }

    @PostMapping("/{username}/items")
	public ResponseEntity<?> addItemsSold(@PathVariable String username, @RequestBody ReceiveListDTO items) {
	    try {
			businessService.addItemsSold(items.information(), username);
			return ResponseEntity.ok().build();
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
			return ResponseEntity.status(403).build();
		} 
	}
	
    @DeleteMapping("/{username}/items")
	public ResponseEntity<?> deleteItemsSold(@PathVariable String username, @RequestBody ReceiveListDTO items) {
	    try {
			businessService.deleteItemsSold(items.information(), username);
			return ResponseEntity.ok().build();
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
			return ResponseEntity.status(403).build();
		} 
	}
	
    @PostMapping("/{username}/countries")
	public ResponseEntity<?> addCountriesSoldTo(@PathVariable String username, @RequestBody ReceiveListDTO countries) {
	    try {
			businessService.addDestinationCountry(countries.information(), username);
			return ResponseEntity.ok().build();
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
			return ResponseEntity.status(403).build();
		} 
	}
	
    @DeleteMapping("/{username}/countries")
	public ResponseEntity<?> deleteCountriesSoldTo(@PathVariable String username, @RequestBody ReceiveListDTO countries) {
	    try {
			businessService.deleteDestinationCountry(countries.information(), username);
			return ResponseEntity.ok().build();
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
			return ResponseEntity.status(403).build();
		} 
	}

    @GetMapping("/test")
    public ResponseEntity<?> testBusiness() {
        return ResponseEntity.ok("trolling");
    }
}
