package com.laundry.pos.service;

import com.laundry.pos.model.Customer;
import com.laundry.pos.repository.CustomerRepository;
import com.laundry.pos.request.CustomerRequest;
import com.laundry.pos.response.CustomerResponse;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(
            CustomerRepository customerRepository
    ) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(
            CustomerRequest request
    ) {

        validateRequest(request);

        String phone =
                request.phone()
                        .trim();

        if (
                customerRepository
                        .existsByPhone(phone)
        ) {
            throw new RuntimeException(
                    "Customer phone already exists"
            );
        }

        Customer customer =
                new Customer();

        customer.setName(
                request.name()
                        .trim()
        );

        customer.setPhone(phone);

        customer.setActive(true);

        Customer savedCustomer =
                customerRepository.save(
                        customer
                );

        return new CustomerResponse(
                true,
                "Customer created successfully",
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getPhone()
        );
    }


    public CustomerResponse getCustomerByPhone(
            String phone
    ) {

        String normalizedPhone =
                phone.trim();

        return customerRepository
                .findByPhone(normalizedPhone)
                .map(customer ->
                        new CustomerResponse(
                                true,
                                "Customer found",
                                customer.getId(),
                                customer.getName(),
                                customer.getPhone()
                        )
                )
                .orElseGet(() ->
                        new CustomerResponse(
                                false,
                                "Customer not found",
                                null,
                                null,
                                normalizedPhone
                        )
                );
    }


    private void validateRequest(
            CustomerRequest request
    ) {

        if (
                request.name() == null ||
                request.name().isBlank()
        ) {
            throw new RuntimeException(
                    "Customer name is required"
            );
        }

        if (
                request.phone() == null ||
                request.phone().isBlank()
        ) {
            throw new RuntimeException(
                    "Customer phone is required"
            );
        }
    }
}