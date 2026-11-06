package com.dahdt.ecommerce.modules.cart.repository;

import com.dahdt.ecommerce.modules.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng cart_items (chi tiết món hàng trong giỏ).
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Tìm dòng sản phẩm trong giỏ theo cartId và variantId.
     */
    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);

    /**
     * Tìm dòng sản phẩm kèm nạp quan hệ cart, user và variant để kiểm tra phân quyền sở hữu.
     */
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.cart c " +
            "JOIN FETCH c.user u " +
            "JOIN FETCH ci.variant v " +
            "JOIN FETCH v.product p " +
            "WHERE ci.id = :id")
    Optional<CartItem> findByIdWithDetails(@Param("id") Long id);
}

