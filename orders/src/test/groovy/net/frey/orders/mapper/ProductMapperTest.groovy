package net.frey.orders.mapper

import net.frey.orders.TestData
import net.frey.orders.data.entity.ProductEntity
import net.frey.orders.model.Product
import spock.lang.Specification

class ProductMapperTest extends Specification {
    def "Correctly maps RO to Entity"() {
        given:
        def productRo = new Product(
            null,
            "Computer",
            new BigDecimal("1000.00")
        )

        when:
        def entity = ProductMapper.toEntity(productRo)

        then:
        entity.name == "Computer"
        entity.price == new BigDecimal("1000.00")
    }

    def "Correctly maps Entity to RO"() {
        given:
        def productEntity = TestData.productEntity()

        when:
        def product = ProductMapper.toRo(productEntity)

        then:
        product.name() == "Computer"
        product.price() == new BigDecimal("1000.00")
    }
}
