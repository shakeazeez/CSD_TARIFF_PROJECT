package com.user.controller;

import com.user.dto.BusinessInfoDTO;
import com.user.service.BusinessService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	
	@GetMapping("/test")
	public ResponseEntity<?> testBusiness() {
	    return ResponseEntity.ok("trolling");
	}
}
