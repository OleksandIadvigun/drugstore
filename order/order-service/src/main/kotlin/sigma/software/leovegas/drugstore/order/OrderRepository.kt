package sigma.software.leovegas.drugstore.order

import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository: JpaRepository<Order,Long>
