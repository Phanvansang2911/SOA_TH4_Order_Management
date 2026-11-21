package com.soa.reporting;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="product_reports",uniqueConstraints=@UniqueConstraint(name="uk_product_per_report",columnNames={"order_report_id","product_id"}))
public class ProductReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Integer id;
 @Column(name="order_report_id",nullable=false) public Integer orderReportId;
 @Column(name="product_id",nullable=false) public Integer productId;
 @Column(name="total_sold",nullable=false) public Integer totalSold;
 @Column(nullable=false,precision=10,scale=2) public BigDecimal revenue;
 @Column(nullable=false,precision=10,scale=2) public BigDecimal cost;
 @Column(nullable=false,precision=10,scale=2) public BigDecimal profit;
 // Snapshot of product stock when the report is generated.
 @Column(name="stock_quantity") public Integer stockQuantity;
}
