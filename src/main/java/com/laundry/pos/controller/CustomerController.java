package com.laundry.pos.controller;

import com.laundry.pos.response.CustomerResponse;
import com.laundry.pos.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService
    ) {
        this.customerService = customerService;
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getByPhone(
            @PathVariable String phone
    ) {
        return ResponseEntity.ok(
                customerService.getByPhone(phone)
        );
    }
}