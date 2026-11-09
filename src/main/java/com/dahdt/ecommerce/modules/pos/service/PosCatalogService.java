package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.modules.pos.dto.PosProductSearchResponseDTO;

import java.util.List;

/**
 * Service tìm kiếm và tra cứu danh mục sản phẩm cho màn hình bán hàng tại quầy POS.
 */
public interface PosCatalogService {

    /**
     * Tìm kiếm biến thể sản phẩm đang hoạt động (ACTIVE) của sản phẩm ACTIVE theo:
     * - Mã SKU (khớp chính xác)
     * - Mã vạch Barcode (khớp chính xác)
     * - Từ khóa trong tên sản phẩm cha
     * Kèm theo số lượng tồn kho thực tế tại CỬA HÀNG CỦA THU NGÂN (không phải kho tổng).
     * Trả về tối đa 20 dòng và không để xảy ra N+1 query.
     *
     * @param userId ID tài khoản thu ngân đang đăng nhập
     * @param keyword Từ khóa tìm kiếm (SKU, barcode hoặc tên sản phẩm)
     * @return Danh sách tối đa 20 dòng PosProductSearchResponseDTO kèm tồn kho cửa hàng
     */
    List<PosProductSearchResponseDTO> search(Long userId, String keyword);
}

