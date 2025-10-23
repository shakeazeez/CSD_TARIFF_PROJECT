package com.user.controller;

import com.user.dto.BankInfoDTO;
import com.user.service.BankService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank")
public class BankController {

	private final BankService bankService;
	private final Logger log = LoggerFactory.getLogger(BankController.class);
	
	public BankController (BankService bankService) {
	    this.bankService = bankService;
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<?> getBankUserDetails(@PathVariable String username) {
	    try {
			BankInfoDTO bankInfoDTO = bankService.getBankInfo(username);
			return ResponseEntity.ok(bankInfoDTO);
		} catch (IllegalAccessError e) {
		    log.info(e.getMessage());
		    return ResponseEntity.status(403).build();
		} catch (IllegalArgumentException e) {
		    log.info(e.getMessage());
		    return ResponseEntity.status(404).build();
		}
	}
	
	@GetMapping("/test")
	public ResponseEntity<?> testBank() {
	    return ResponseEntity.ok("trolling");
	}
}