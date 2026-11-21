package com.soa.reporting;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="orders_reports",uniqueConstraints=@UniqueConstraint(name="uk_report_order",columnNames="order_id"))
public class OrderReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Integer id;
 @Column(name="order_id",nullable=false) public Integer orderId;
 @Column(name="total_revenue",nullable=false,precision=10,scale=2) public BigDecimal totalRevenue=BigDecimal.ZERO;
 @Column(name="total_cost",nullable=false,precision=10,scale=2) public BigDecimal totalCost=BigDecimal.ZERO;
 @Column(name="total_profit",nullable=false,precision=10,scale=2) public BigDecimal totalProfit=BigDecimal.ZERO;
}
