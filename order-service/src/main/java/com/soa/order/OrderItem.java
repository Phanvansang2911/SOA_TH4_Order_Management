package com.soa.order;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="order_items") public class OrderItem{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Integer id;
 @Column(name="order_id",nullable=false) public Integer orderId;
 @Column(name="product_id",nullable=false) public Integer productId;
 @Column(name="product_name",nullable=false) public String productName;
 @Column(nullable=false) public Integer quantity;
 @Column(name="unit_price",nullable=false,precision=10,scale=2) public BigDecimal unitPrice;
 @Column(name="total_price",nullable=false,precision=10,scale=2) public BigDecimal totalPrice;
}
