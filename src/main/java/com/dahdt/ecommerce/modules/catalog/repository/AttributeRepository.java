package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
}
