package sigma.software.leovegas.drugstore.service;

import java.util.List;
import sigma.software.leovegas.drugstore.dto.ProductDto;

public interface ProductServiceI {

    public List<ProductDto> getAll();

    public ProductDto getOne(Long id);

    public ProductDto create (ProductDto productDto);

    public ProductDto update (Long id, ProductDto productDto);

    public void delete (Long id);

}
