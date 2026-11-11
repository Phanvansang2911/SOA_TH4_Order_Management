package com.soa.order;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@Entity @Table(name="orders") public class OrderEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Integer id;
 @Column(name="customer_name",nullable=false) public String customerName;
 @Column(name="customer_email",nullable=false) public String customerEmail;
 @Column(name="total_amount",nullable=false,precision=10,scale=2) public BigDecimal totalAmount=BigDecimal.ZERO;
 @Column(nullable=false,length=50) public String status="pending";
 @Column(name="created_at",nullable=false,updatable=false) public LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) public LocalDateTime updatedAt;
 @PrePersist void init(){createdAt=LocalDateTime.now();updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=LocalDateTime.now();}
}
