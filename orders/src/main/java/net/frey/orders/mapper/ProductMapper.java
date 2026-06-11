package net.frey.orders.mapper;

import net.frey.orders.data.entity.ProductEntity;
import net.frey.orders.model.Product;

public class ProductMapper {
    private ProductMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ProductEntity toEntity(Product product) {
        var entity = new ProductEntity();
        entity.setName(product.name());
        entity.setPrice(product.price());

        return entity;
    }

    public static Product toRo(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getPrice());
    }
}
