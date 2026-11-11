package com.soa.auth;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="login_tokens") public class LoginToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=64) public String tokenHash;
 @ManyToOne(optional=false) public UserAccount user;
 @Column(nullable=false) public Instant expiresAt;
 public LoginToken(){} public LoginToken(String hash,UserAccount user,Instant expiresAt){this.tokenHash=hash;this.user=user;this.expiresAt=expiresAt;}
}
