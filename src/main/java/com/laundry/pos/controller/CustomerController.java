package com.laundry.pos.controller;

import com.laundry.pos.request.CustomerRequest;
import com.laundry.pos.response.CustomerResponse;
import com.laundry.pos.service.CustomerService;
import org.springframework.http.HttpStatus;
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


    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody CustomerRequest request
    ) {

        CustomerResponse response =
                customerService.createCustomer(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getCustomerByPhone(
            @PathVariable String phone
    ) {

        return ResponseEntity.ok(
                customerService.getCustomerByPhone(
                        phone
                )
        );
    }
}