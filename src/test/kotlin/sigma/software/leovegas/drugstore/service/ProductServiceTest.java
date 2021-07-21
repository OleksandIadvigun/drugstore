package sigma.software.leovegas.drugstore.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sigma.software.leovegas.drugstore.dto.ProductDto;
import sigma.software.leovegas.drugstore.exception.ResourceNotFoundException;
import sigma.software.leovegas.drugstore.persistence.entity.Product;
import sigma.software.leovegas.drugstore.persistence.repository.ProductRepository;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
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

    @InjectMocks
    private ProductServiceImpl service;

    @Mock
    private ProductRepository repo;

    @Test
    public void should_be_created_new_Product() {
        when(repo.save(product)).thenReturn(product);

        ProductDto actual = service.create(productDto);
        Assertions.assertEquals(productDto, actual);
    }

    @Test
    public void IfSaveEmptyProd_shouldReturnException() {
        when(repo.save(null)).thenThrow(NullPointerException.class);

        Assertions.assertThrows(NullPointerException.class, () -> {
            repo.save(null);
        });
    }

    @Test
    public void should_return_All_products() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        List<ProductDto> productsDto = new ArrayList<>();
        productsDto.add(productDto);
        when(repo.findAll()).thenReturn(products);

        List<ProductDto> actual = service.getAll();
        Assertions.assertEquals(productsDto, actual);
    }

    @Test
    public void ifExist_should_returnOneProduct() {
        Long existID = 12L;
        when(repo.findById(existID)).thenReturn(java.util.Optional.ofNullable(product));

        ProductDto actual = service.getOne(existID);
        Assertions.assertEquals(productDto, actual);
    }

    @Test
    public void ifNotExist_shouldReturn_ResourceNotFoundException(){
        Long notExistID = 20L;
        when(repo.findById(notExistID)).thenThrow(ResourceNotFoundException.class);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.getOne(notExistID);
        });
    }

}
