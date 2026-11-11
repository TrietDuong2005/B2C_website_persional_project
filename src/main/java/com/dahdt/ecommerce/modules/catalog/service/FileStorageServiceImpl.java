package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Cài đặt nghiệp vụ lưu trữ tệp tin hình ảnh an toàn.
 * Bảo vệ chống path traversal, kiểm tra đuôi mở rộng, MIME type và chữ ký magic bytes.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb:2}")
    private int maxSizeMb;

    public FileStorageServiceImpl() {
    }

    /**
     * Constructor phục vụ kiểm thử đơn vị hoặc khởi tạo tùy biến.
     */
    public FileStorageServiceImpl(String uploadDir, int maxSizeMb) {
        this.uploadDir = uploadDir;
        this.maxSizeMb = maxSizeMb;
    }

    @Override
    public String storeProductImage(MultipartFile file) {
        // 1. Kiểm tra tệp không được rỗng
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Tệp ảnh tải lên không được để trống");
        }

        // 2. Kiểm tra kích thước tối đa (tính theo MB)
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("Dung lượng tệp (" + (file.getSize() / 1024) + " KB) vượt quá giới hạn tối đa cho phép (" + maxSizeMb + " MB)");
        }

        // 3. Kiểm tra phần mở rộng tệp
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BadRequestException("Tệp tải lên không có phần mở rộng hợp lệ");
        }

        String rawExt = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_EXTENSIONS.contains(rawExt)) {
            throw new BadRequestException("Định dạng tệp '." + rawExt + "' không được hỗ trợ. Chỉ chấp nhận các định dạng: JPG, JPEG, PNG, WEBP");
        }

        // 4. Kiểm tra Content-Type tương ứng
        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(rawExt, contentType.toLowerCase(Locale.ROOT).trim())) {
            throw new BadRequestException("Kiểu nội dung (Content-Type: " + contentType + ") không khớp với phần mở rộng tệp '." + rawExt + "'");
        }

        // 5. Kiểm tra chữ ký byte đầu tệp (magic bytes)
        byte[] header = new byte[12];
        int bytesRead;
        try (InputStream is = file.getInputStream()) {
            bytesRead = is.read(header);
        } catch (IOException e) {
            log.error("Lỗi khi đọc stream tệp tải lên: {}", e.getMessage());
            throw new BadRequestException("Không thể đọc nội dung tệp tải lên: " + e.getMessage());
        }

        if (bytesRead < 3 || !isValidMagicBytes(rawExt, header, bytesRead)) {
            log.warn("Từ chối tệp '{}' vì chữ ký magic bytes không khớp với định dạng ảnh thật", originalFilename);
            throw new BadRequestException("Chữ ký tệp (magic bytes) không khớp với ảnh thật. Tệp có thể đã bị đổi đuôi hoặc không hợp lệ");
        }

        // 6. KHÔNG tin tên tệp gốc: Tên lưu là UUID cộng đuôi đã kiểm chứng
        String verifiedExt = "jpeg".equals(rawExt) ? "jpg" : rawExt;
        String safeFileName = UUID.randomUUID().toString() + "." + verifiedExt;

        // 7. Chuẩn hóa đường dẫn và bảo đảm tệp nằm trong thư mục upload (chống path traversal)
        Path rootUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetDirPath = rootUploadPath.resolve("products").normalize();

        if (!targetDirPath.startsWith(rootUploadPath)) {
            throw new BadRequestException("Đường dẫn lưu trữ không hợp lệ");
        }

        try {
            if (!Files.exists(targetDirPath)) {
                Files.createDirectories(targetDirPath);
            }

            Path destinationPath = targetDirPath.resolve(safeFileName).normalize();
            if (!destinationPath.startsWith(targetDirPath)) {
                throw new BadRequestException("Phát hiện đường dẫn không an toàn (path traversal)");
            }

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Lưu tệp ảnh sản phẩm thành công: {}", destinationPath);
        } catch (IOException e) {
            log.error("Lỗi khi ghi tệp lên đĩa: {}", e.getMessage(), e);
            throw new BadRequestException("Lỗi trong quá trình lưu trữ tệp: " + e.getMessage());
        }

        // 8. Trả về đường dẫn công khai
        return "/uploads/products/" + safeFileName;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String cleanUrl = fileUrl.trim().replace('\\', '/');

        // Chỉ xóa nếu URL thuộc thư mục uploads của hệ thống
        if (!cleanUrl.startsWith("/uploads/") && !cleanUrl.startsWith("uploads/")) {
            log.debug("Bỏ qua xóa ảnh vì không thuộc thư mục uploads: {}", fileUrl);
            return;
        }

        String subPath = cleanUrl.startsWith("/uploads/")
                ? cleanUrl.substring("/uploads/".length())
                : cleanUrl.substring("uploads/".length());

        Path rootUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path targetFilePath = rootUploadPath.resolve(subPath).normalize();

        // Bảo đảm an toàn: đường dẫn chuẩn hóa phải bắt đầu bằng thư mục upload
        if (!targetFilePath.startsWith(rootUploadPath)) {
            log.warn("Cảnh báo bảo mật: Phát hiện cố gắng xóa tệp nằm ngoài thư mục uploads: {}", fileUrl);
            return;
        }

        try {
            if (Files.exists(targetFilePath) && Files.isRegularFile(targetFilePath)) {
                Files.delete(targetFilePath);
                log.info("Đã xóa tệp vật lý trong thư mục uploads: {}", targetFilePath);
            }
        } catch (IOException e) {
            log.error("Lỗi khi xóa tệp vật lý {}: {}", targetFilePath, e.getMessage());
        }
    }

    // =========================================================================
    // HÀM KIỂM TRA PHỤ TRỢ (MIME TYPE & MAGIC BYTES)
    // =========================================================================

    private boolean isValidContentType(String ext, String contentType) {
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg".equals(contentType)
                    || "image/jpg".equals(contentType)
                    || "image/pjpeg".equals(contentType);
            case "png" -> "image/png".equals(contentType)
                    || "image/x-png".equals(contentType);
            case "webp" -> "image/webp".equals(contentType);
            default -> false;
        };
    }

    private boolean isValidMagicBytes(String ext, byte[] header, int bytesRead) {
        return switch (ext) {
            case "jpg", "jpeg" ->
                // JPEG: FF D8 FF
                    bytesRead >= 3
                            && (header[0] & 0xFF) == 0xFF
                            && (header[1] & 0xFF) == 0xD8
                            && (header[2] & 0xFF) == 0xFF;

            case "png" ->
                // PNG: 89 50 4E 47 0D 0A 1A 0A
                    bytesRead >= 8
                            && (header[0] & 0xFF) == 0x89
                            && header[1] == 0x50 // P
                            && header[2] == 0x4E // N
                            && header[3] == 0x47 // G
                            && header[4] == 0x0D // \r
                            && header[5] == 0x0A // \n
                            && header[6] == 0x1A
                            && header[7] == 0x0A; // \n

            case "webp" ->
                // WEBP: RIFF....WEBP
                    bytesRead >= 12
                            && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                            && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';

            default -> false;
        };
    }
}

