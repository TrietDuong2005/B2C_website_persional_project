package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.modules.store.dto.StoreRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.StoreResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface nghiệp vụ quản lý Điểm bán lẻ và Kho bãi (StoreService).
 */
public interface StoreService {

    /**
     * Lấy danh sách chi nhánh có phân trang và bộ lọc đa điều kiện.
     */
    Page<StoreResponseDTO> getStores(String keyword, StoreType type, StoreStatus status, Pageable pageable);

    /**
     * Lấy chi tiết thông tin chi nhánh theo ID.
     */
    StoreResponseDTO getStoreById(Long id);

    /**
     * Lấy thông tin chi nhánh theo mã code.
     */
    StoreResponseDTO getStoreByCode(String code);

    /**
     * Lấy toàn bộ danh sách các chi nhánh đang hoạt động (dùng cho dropdown chọn cửa hàng).
     */
    List<StoreResponseDTO> getActiveStores();

    /**
     * Tạo mới một chi nhánh hoặc kho bãi.
     * Kiểm tra mã code là duy nhất, tọa độ GPS hợp lệ.
     */
    StoreResponseDTO createStore(StoreRequestDTO dto);

    /**
     * Cập nhật thông tin chi nhánh.
     */
    StoreResponseDTO updateStore(Long id, StoreRequestDTO dto);

    /**
     * Ngưng hoạt động chi nhánh ("Xóa mềm", chuyển status sang INACTIVE).
     */
    void deactivateStore(Long id);

    /**
     * Kích hoạt lại chi nhánh (chuyển status sang ACTIVE).
     */
    void activateStore(Long id);
}

