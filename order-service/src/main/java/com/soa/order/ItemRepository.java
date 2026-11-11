package com.soa.order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ItemRepository extends JpaRepository<OrderItem,Integer>{
 List<OrderItem> findByOrderId(Integer orderId);
 void deleteByOrderId(Integer orderId);
}
