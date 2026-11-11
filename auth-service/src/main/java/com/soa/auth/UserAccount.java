package com.soa.auth;
import jakarta.persistence.*;
@Entity @Table(name="user_accounts")
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Integer id;
 @Column(nullable=false,unique=true,length=255) public String email;
 @Column(nullable=false) public String passwordHash;
 public UserAccount(){} public UserAccount(String email,String hash){this.email=email;this.passwordHash=hash;}
}
