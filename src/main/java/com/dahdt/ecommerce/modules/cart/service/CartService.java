package com.dahdt.ecommerce.modules.cart.service;

import com.dahdt.ecommerce.modules.cart.dto.CartResponseDTO;

/**
 * Interface nghiệp vụ giỏ hàng trực tuyến (CartService).
 */
public interface CartService {

    /**
     * Lấy toàn bộ thông tin giỏ hàng ACTIVE của người dùng kèm cờ kiểm tra tồn kho.
     *
     * @param userId ID người dùng đang đăng nhập (CUSTOMER)
     * @return CartResponseDTO chứa danh sách món hàng, tạm tính và cờ cảnh báo
     */
    CartResponseDTO getCart(Long userId);

    /**
     * Thêm biến thể sản phẩm vào giỏ hàng:
     * - Chỉ thêm biến thể ACTIVE của sản phẩm ACTIVE
     * - Cộng dồn nếu biến thể đã có trong giỏ
     * - Số lượng mỗi dòng từ 1 đến 10
     * - Tổng số lượng không được vượt tồn online
     * - Giá bán luôn lấy từ DB
     *
     * @param userId ID người dùng
     * @param variantId ID biến thể sản phẩm
     * @param quantity Số lượng thêm vào (1..10)
     */
    void addItem(Long userId, Long variantId, int quantity);

    /**
     * Sửa số lượng của một dòng sản phẩm trong giỏ:
     * - Kiểm tra quyền sở hữu giỏ (sai chủ thì ném AccessDeniedException)
     * - Kiểm tra số lượng hợp lệ (1..10) và không vượt tồn online
     *
     * @param userId ID người dùng
     * @param itemId ID dòng CartItem
     * @param quantity Số lượng mới
     */
    void updateItemQuantity(Long userId, Long itemId, int quantity);

    /**
     * Xóa một dòng sản phẩm khỏi giỏ:
     * - Kiểm tra quyền sở hữu giỏ (sai chủ thì ném AccessDeniedException)
     *
     * @param userId ID người dùng
     * @param itemId ID dòng CartItem
     */
    void removeItem(Long userId, Long itemId);

    /**
     * Xóa toàn bộ giỏ hàng của người dùng.
     *
     * @param userId ID người dùng
     */
    void clearCart(Long userId);

    /**
     * Đếm tổng số lượng món hàng trong giỏ của người dùng (phục vụ hiển thị badge giỏ hàng trên navbar).
     *
     * @param userId ID người dùng
     * @return Số lượng món hàng trong giỏ
     */
    int countActiveCartItems(Long userId);
}

