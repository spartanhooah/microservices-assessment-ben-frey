package net.frey.orders.data.repository;

import java.util.UUID;
import net.frey.orders.data.entity.CustomerEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends CrudRepository<CustomerEntity, UUID> {}
