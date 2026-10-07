package com.dahdt.ecommerce.modules.auth.repository;

import com.dahdt.ecommerce.modules.auth.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
}
