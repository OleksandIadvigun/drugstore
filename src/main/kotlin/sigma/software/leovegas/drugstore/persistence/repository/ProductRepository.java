package sigma.software.leovegas.drugstore.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sigma.software.leovegas.drugstore.persistence.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
