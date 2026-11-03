package com.dahdt.ecommerce.modules.catalog.repository;

import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndIsDeletedFalse(Long productId);
}
