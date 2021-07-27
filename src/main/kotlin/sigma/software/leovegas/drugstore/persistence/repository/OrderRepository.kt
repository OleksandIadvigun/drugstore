package sigma.software.leovegas.drugstore.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import sigma.software.leovegas.drugstore.persistence.entity.Order

interface OrderRepository: JpaRepository<Order,Long> {
}
