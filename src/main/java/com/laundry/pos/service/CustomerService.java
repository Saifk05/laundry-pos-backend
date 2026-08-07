package com.laundry.pos.service;

import com.laundry.pos.model.Customer;
import com.laundry.pos.repository.CustomerRepository;
import com.laundry.pos.response.CustomerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(
            CustomerRepository customerRepository
    ) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse getByPhone(
            String phone
    ) {

        Customer customer = customerRepository
                .findByPhoneAndActiveTrue(phone)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found"
                        )
                );

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone()
        );
    }
}