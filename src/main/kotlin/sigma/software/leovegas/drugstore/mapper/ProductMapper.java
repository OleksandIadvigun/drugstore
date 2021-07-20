package sigma.software.leovegas.drugstore.mapper;

import sigma.software.leovegas.drugstore.dto.ProductDto;
import sigma.software.leovegas.drugstore.persistence.entity.Product;

public class ProductMapper {
    public static ProductDto convertFromProductToProductDTO(Product product){
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public static Product convertFromProductDTOToProduct(ProductDto product){
        return Product.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}
