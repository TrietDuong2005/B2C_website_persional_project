package com.dahdt.ecommerce.modules.inventory.service;

import com.dahdt.ecommerce.modules.inventory.dto.*;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * Interface nghiệp vụ quản lý Tồn kho và Sổ cái giao dịch kho (InventoryService).
 */
public interface InventoryService {

    /**
     * Xem danh sách tồn kho theo từng chi nhánh:
     * - SKU, tên sản phẩm, màu, size, số lượng tồn, mức đặt lại hàng, trạng thái (HẾT / SẮP HẾT / ĐỦ)
     * - Bộ lọc: từ khóa (SKU, tên, barcode), danh mục, trạng thái tồn
     * - Phân trang, tối ưu tránh N+1 query bằng JOIN FETCH
     */
    Page<InventoryResponseDTO> getStoreInventory(Long storeId, String keyword, Long categoryId, String stockStatus, Pageable pageable);

    /**
     * Lấy chi tiết thông tin tồn kho theo ID.
     */
    InventoryResponseDTO getInventoryById(Long id);

    /**
     * Lấy thông tin tồn kho của một biến thể tại một chi nhánh cụ thể.
     */
    InventoryResponseDTO getInventoryByStoreAndVariant(Long storeId, Long variantId);

    /**
     * Thao tác Nhập kho (IMPORT):
     * - Số lượng nhập phải > 0
     * - Cập nhật tăng số lượng tồn kho (inventory.increase)
     * - Ghi MỘT bản ghi vào sổ cái inventory_transactions (type: IMPORT, reference_type: MANUAL)
     * - Cùng nằm trong một giao dịch @Transactional
     */
    InventoryResponseDTO importStock(InventoryImportRequestDTO dto, Long userId);

    /**
     * Thao tác Điều chỉnh kho (ADJUST):
     * - Số lượng thay đổi có thể âm hoặc dương (khác 0)
     * - Ghi chú lý do điều chỉnh là BẮT BUỘC
     * - Không cho phép tồn kho âm (ném InsufficientStockException nếu trừ quá số tồn)
     * - Ghi MỘT bản ghi vào sổ cái inventory_transactions (type: ADJUST, reference_type: MANUAL)
     * - Cùng nằm trong một giao dịch @Transactional
     */
    InventoryResponseDTO adjustStock(InventoryAdjustRequestDTO dto, Long userId);

    /**
     * Cập nhật ngưỡng đặt lại hàng (reorder_level) cho một dòng tồn kho cụ thể.
     */
    InventoryResponseDTO updateReorderLevel(InventoryReorderLevelRequestDTO dto);

    /**
     * Xem sổ cái giao dịch kho (Audit Log):
     * - Lọc theo chi nhánh, mã SKU / tên sản phẩm, loại giao dịch, khoảng ngày
     * - Sắp xếp mặc định: mới nhất lên trước
     * - Có phân trang
     * - Sổ kho chỉ thêm, tuyệt đối không sửa hay xóa
     */
    Page<InventoryTransactionResponseDTO> getLedger(Long storeId, String sku, InventoryTransactionType type,
                                                    LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);
}

