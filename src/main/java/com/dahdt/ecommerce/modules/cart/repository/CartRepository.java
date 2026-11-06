package com.dahdt.ecommerce.modules.cart.repository;

import com.dahdt.ecommerce.modules.cart.entity.Cart;
import com.dahdt.ecommerce.modules.cart.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng carts (giỏ hàng trực tuyến).
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Tìm giỏ hàng của người dùng theo trạng thái (thường là ACTIVE).
     */
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    /**
     * Tìm giỏ hàng kèm fetch các dòng cart_items, biến thể, sản phẩm và thương hiệu (tránh LazyInitializationException và N+1).
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
            "LEFT JOIN FETCH c.items i " +
            "LEFT JOIN FETCH i.variant v " +
            "LEFT JOIN FETCH v.product p " +
            "LEFT JOIN FETCH p.brand b " +
            "WHERE c.user.id = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatusWithDetails(@Param("userId") Long userId, @Param("status") CartStatus status);

    /**
     * Đếm tổng số lượng món hàng trong giỏ hàng ACTIVE của người dùng (phục vụ hiển thị badge giỏ hàng trên navbar).
     */
    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Cart c JOIN c.items i WHERE c.user.id = :userId AND c.status = com.dahdt.ecommerce.modules.cart.entity.CartStatus.ACTIVE")
    int countActiveCartItemQuantity(@Param("userId") Long userId);
}

