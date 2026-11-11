package com.dahdt.ecommerce.modules.catalog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface nghiệp vụ lưu trữ và quản lý tệp tin an toàn (hình ảnh sản phẩm).
 */
public interface FileStorageService {

    /**
     * Lưu trữ tệp ảnh sản phẩm tải lên sau khi kiểm tra tính hợp lệ và an toàn.
     *
     * @param file tệp multipart tải lên từ client
     * @return đường dẫn công khai của ảnh (/uploads/products/<uuid>.<ext>)
     */
    String storeProductImage(MultipartFile file);

    /**
     * Xóa tệp vật lý nếu nằm trong thư mục upload.
     * Bỏ qua các đường dẫn ảnh tĩnh /images/... hoặc URL ngoài (http/https).
     *
     * @param fileUrl đường dẫn ảnh cần xóa
     */
    void deleteFile(String fileUrl);
}

