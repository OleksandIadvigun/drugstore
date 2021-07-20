package sigma.software.leovegas.drugstore.mapper;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sigma.software.leovegas.drugstore.dto.ProductDto;
import sigma.software.leovegas.drugstore.persistence.entity.Product;


public class ProductMapperTest {
    BigDecimal bigDecimal = new BigDecimal("22.5");
    ProductDto productDto = ProductDto.builder()
            .name("my")
            .quantity(2)
            .price(bigDecimal)
            .build();
    Product product = Product.builder()
            .name("my")
            .quantity(2)
            .price(bigDecimal)
            .build();

    @Test
    public void should_convert_ProductDTO_to_Product() {
        Product actual = ProductMapper.convertFromProductDTOToProduct(productDto);
        Assertions.assertEquals(product, actual);
    }

    @Test
    public void should_convert_Product_to_ProductDTO() {
        ProductDto actual = ProductMapper.convertFromProductToProductDTO(product);
        Assertions.assertEquals(productDto, actual);
    }
}
