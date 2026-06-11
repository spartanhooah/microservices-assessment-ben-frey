package net.frey.orders.controller;

import lombok.RequiredArgsConstructor;
import net.frey.orders.model.Customer;
import net.frey.orders.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> save(@RequestBody Customer customer) {
        var savedCustomer = customerService.saveCustomer(customer);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }
}
