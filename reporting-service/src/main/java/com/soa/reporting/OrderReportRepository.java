package com.soa.reporting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface OrderReportRepository extends JpaRepository<OrderReport,Integer>{Optional<OrderReport> findByOrderId(Integer orderId);}
