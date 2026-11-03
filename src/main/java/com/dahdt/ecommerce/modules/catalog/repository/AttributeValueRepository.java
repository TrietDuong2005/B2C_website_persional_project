package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
}
