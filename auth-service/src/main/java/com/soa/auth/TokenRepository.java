package com.soa.auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TokenRepository extends JpaRepository<LoginToken,Long>{Optional<LoginToken> findByTokenHash(String hash);}
