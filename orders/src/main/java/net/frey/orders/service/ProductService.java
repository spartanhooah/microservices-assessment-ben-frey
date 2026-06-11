package net.frey.orders.service;

import lombok.RequiredArgsConstructor;
import net.frey.orders.data.repository.ProductRepository;
import net.frey.orders.mapper.ProductMapper;
import net.frey.orders.model.Product;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product saveProduct(Product product) {
        var entity = ProductMapper.toEntity(product);

        var savedEntity = productRepository.save(entity);

        return ProductMapper.toRo(savedEntity);
    }
}
