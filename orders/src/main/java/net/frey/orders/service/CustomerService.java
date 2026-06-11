package net.frey.orders.service;

import lombok.RequiredArgsConstructor;
import net.frey.orders.data.repository.CustomerRepository;
import net.frey.orders.mapper.CustomerMapper;
import net.frey.orders.model.Customer;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository repository;

    public Customer saveCustomer(Customer customer) {
        var entity = CustomerMapper.toEntity(customer);

        var savedEntity = repository.save(entity);

        return CustomerMapper.toRo(savedEntity);
    }
}
