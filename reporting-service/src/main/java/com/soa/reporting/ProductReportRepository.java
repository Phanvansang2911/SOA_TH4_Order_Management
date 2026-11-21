package com.soa.reporting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ProductReportRepository extends JpaRepository<ProductReport,Integer>{
 List<ProductReport> findByOrderReportId(Integer orderReportId);
 Optional<ProductReport> findByOrderReportIdAndProductId(Integer orderReportId,Integer productId);
 void deleteByOrderReportId(Integer orderReportId);
}
