package com.dahdt.ecommerce.modules.promotion.repository;

import com.dahdt.ecommerce.modules.promotion.entity.Voucher;
import com.dahdt.ecommerce.modules.promotion.entity.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository truy xuất và thao tác dữ liệu voucher khuyến mãi (bảng vouchers).
 */
@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    /**
     * Tìm voucher theo mã code.
     */
    Optional<Voucher> findByCode(String code);

    /**
     * Tìm voucher theo mã code và trạng thái hoạt động.
     */
    Optional<Voucher> findByCodeAndStatus(String code, VoucherStatus status);

    /**
     * Tăng số lượt sử dụng used_count của voucher bằng một câu UPDATE có điều kiện nguyên tử.
     * Giải quyết triệt để tranh chấp đồng thời (race condition) khi nhiều khách hàng cùng áp dụng
     * một mã giảm giá có giới hạn lượt tại cùng một thời điểm mà không sợ vượt quá usage_limit.
     *
     * @param voucherId ID của voucher
     * @param status Trạng thái yêu cầu (thường là ACTIVE)
     * @param now Thời điểm kiểm tra hiệu lực
     * @return Số bản ghi được cập nhật thành công (nếu = 0 nghĩa là đã hết lượt hoặc hết hạn)
     */
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1, v.updatedAt = :now " +
            "WHERE v.id = :voucherId " +
            "AND v.status = :status " +
            "AND v.startAt <= :now AND v.endAt >= :now " +
            "AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    int incrementUsedCountConditionally(@Param("voucherId") Long voucherId,
                                        @Param("status") VoucherStatus status,
                                        @Param("now") LocalDateTime now);
}

