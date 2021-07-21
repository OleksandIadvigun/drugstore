package sigma.software.leovegas.drugstore.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sigma.software.leovegas.drugstore.dto.ProductDto;
import sigma.software.leovegas.drugstore.exception.ResourceNotFoundException;
import sigma.software.leovegas.drugstore.mapper.ProductMapper;
import sigma.software.leovegas.drugstore.persistence.entity.Product;
import sigma.software.leovegas.drugstore.persistence.repository.ProductRepository;

@AllArgsConstructor
@Service
@Transactional
public class ProductServiceImpl implements ProductServiceI {
    private final ProductRepository repo;
    private final String exceptionMessage = "This product with id: %d doesn't exist!";

    @Override
    public List<ProductDto> getAll() {
        return repo.findAll().stream().map(ProductMapper::convertFromProductToProductDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDto getOne(Long id) {
        Optional<Product> optional = repo.findById(id);
        if (optional.isPresent()) {
            return ProductMapper.convertFromProductToProductDTO(optional.get());
        } else throw new ResourceNotFoundException(String.format(exceptionMessage, id));
    }

    @Override
    public ProductDto create(ProductDto productDto) {
        if (productDto != null) {
            Product product = ProductMapper.convertFromProductDTOToProduct(productDto);
            Product savedProduct = repo.save(product);
            return ProductMapper.convertFromProductToProductDTO(savedProduct);
        }
        return null;
    }

    @Override
    public ProductDto update(Long id, ProductDto productDto) {
        Product product = ProductMapper.convertFromProductDTOToProduct(productDto);
        Optional<Product> optional = repo.findById(id);
        if (optional.isPresent()) {
            Product productFromDB = optional.get();
            BeanUtils.copyProperties(product, productFromDB, "id");
            return ProductMapper.convertFromProductToProductDTO(productFromDB);
        } else throw new ResourceNotFoundException(String.format(exceptionMessage, id));
    }

    @Override
    public void delete(Long id) {
        if (repo.findById(id).isPresent()) {
            repo.deleteById(id);
        } else throw new ResourceNotFoundException(String.format(exceptionMessage, id));
    }
}
