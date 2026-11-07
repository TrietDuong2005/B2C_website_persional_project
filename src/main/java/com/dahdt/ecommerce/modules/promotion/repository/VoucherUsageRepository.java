package com.dahdt.ecommerce.modules.promotion.repository;

import com.dahdt.ecommerce.modules.promotion.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository lưu vết và truy xuất lịch sử sử dụng voucher (bảng voucher_usages).
 */
@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    List<VoucherUsage> findByOrderId(Long orderId);

    List<VoucherUsage> findByUserId(Long userId);

    List<VoucherUsage> findByVoucherId(Long voucherId);
}

