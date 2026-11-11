package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử đơn vị lớp lưu trữ tệp an toàn (FileStorageService)")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl fileStorageService;

    // Header byte mẫu hợp lệ
    private static final byte[] VALID_JPG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46};
    private static final byte[] VALID_PNG_BYTES = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
    private static final byte[] VALID_WEBP_BYTES = new byte[]{'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8'};

    @BeforeEach
    void setUp() {
        // Khởi tạo service với thư mục tạm @TempDir và kích thước tối đa 2MB
        fileStorageService = new FileStorageServiceImpl(tempDir.toString(), 2);
    }

    @Test
    @DisplayName("Lưu tệp JPG hợp lệ: Lưu thành công, trả về đường dẫn /uploads/products/<uuid>.jpg")
    void testStoreValidJpg() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "test-shoe.jpg", "image/jpeg", VALID_JPG_BYTES
        );

        String publicUrl = fileStorageService.storeProductImage(file);

        assertNotNull(publicUrl, "Đường dẫn trả về không được null");
        assertTrue(publicUrl.startsWith("/uploads/products/"), "Đường dẫn phải bắt đầu bằng /uploads/products/. Thực tế: " + publicUrl);
        assertTrue(publicUrl.endsWith(".jpg"), "Đường dẫn phải có đuôi .jpg. Thực tế: " + publicUrl);

        String fileName = publicUrl.substring("/uploads/products/".length());
        Path physicalFile = tempDir.resolve("products").resolve(fileName);
        assertTrue(Files.exists(physicalFile), "Tệp vật lý phải tồn tại trên đĩa tại: " + physicalFile);
    }

    @Test
    @DisplayName("Lưu tệp PNG hợp lệ: Lưu thành công, trả về đường dẫn /uploads/products/<uuid>.png")
    void testStoreValidPng() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "test-sneaker.png", "image/png", VALID_PNG_BYTES
        );

        String publicUrl = fileStorageService.storeProductImage(file);

        assertNotNull(publicUrl, "Đường dẫn trả về không được null");
        assertTrue(publicUrl.startsWith("/uploads/products/"), "Đường dẫn phải bắt đầu bằng /uploads/products/. Thực tế: " + publicUrl);
        assertTrue(publicUrl.endsWith(".png"), "Đường dẫn phải có đuôi .png. Thực tế: " + publicUrl);

        String fileName = publicUrl.substring("/uploads/products/".length());
        Path physicalFile = tempDir.resolve("products").resolve(fileName);
        assertTrue(Files.exists(physicalFile), "Tệp vật lý phải tồn tại trên đĩa tại: " + physicalFile);
    }

    @Test
    @DisplayName("Lưu tệp WEBP hợp lệ: Lưu thành công, trả về đường dẫn /uploads/products/<uuid>.webp")
    void testStoreValidWebp() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "test-sandal.webp", "image/webp", VALID_WEBP_BYTES
        );

        String publicUrl = fileStorageService.storeProductImage(file);

        assertNotNull(publicUrl, "Đường dẫn trả về không được null");
        assertTrue(publicUrl.startsWith("/uploads/products/"), "Đường dẫn phải bắt đầu bằng /uploads/products/. Thực tế: " + publicUrl);
        assertTrue(publicUrl.endsWith(".webp"), "Đường dẫn phải có đuôi .webp. Thực tế: " + publicUrl);

        String fileName = publicUrl.substring("/uploads/products/".length());
        Path physicalFile = tempDir.resolve("products").resolve(fileName);
        assertTrue(Files.exists(physicalFile), "Tệp vật lý phải tồn tại trên đĩa tại: " + physicalFile);
    }

    @Test
    @DisplayName("Từ chối tệp có đuôi không nằm trong danh sách cho phép (ví dụ .pdf, .exe)")
    void testStoreInvalidExtension() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "imageFile", "document.pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> fileStorageService.storeProductImage(pdfFile),
                "Phải ném BadRequestException khi tải lên tệp có đuôi không hợp lệ");
        assertTrue(ex.getMessage().contains("không được hỗ trợ"),
                "Thông điệp phải nêu rõ định dạng không được hỗ trợ. Thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("Từ chối tệp có chữ ký magic bytes giả mạo (ví dụ .exe đổi tên thành .jpg)")
    void testStoreWrongMagicBytes() {
        // Tệp DOS/Windows Executable bắt đầu bằng 'M', 'Z' (0x4D, 0x5A) nhưng đặt tên .jpg
        byte[] fakeExeBytes = new byte[]{'M', 'Z', 0x00, 0x01, 0x02, 0x03, 0x04};
        MockMultipartFile fakeFile = new MockMultipartFile(
                "imageFile", "virus-payload.jpg", "image/jpeg", fakeExeBytes
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> fileStorageService.storeProductImage(fakeFile),
                "Phải ném BadRequestException khi tệp có chữ ký byte không khớp loại ảnh thật");
        assertTrue(ex.getMessage().contains("magic bytes"),
                "Thông điệp phải cảnh báo chữ ký magic bytes không khớp. Thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("Từ chối tệp vượt quá kích thước tối đa cho phép (2MB)")
    void testStoreTooLarge() {
        // Tạo mảng byte vượt quá 2MB (2 * 1024 * 1024 + 1 bytes)
        int size = 2 * 1024 * 1024 + 10;
        byte[] largeBytes = new byte[size];
        System.arraycopy(VALID_JPG_BYTES, 0, largeBytes, 0, VALID_JPG_BYTES.length);

        MockMultipartFile largeFile = new MockMultipartFile(
                "imageFile", "large-photo.jpg", "image/jpeg", largeBytes
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> fileStorageService.storeProductImage(largeFile),
                "Phải ném BadRequestException khi kích thước tệp vượt quá giới hạn");
        assertTrue(ex.getMessage().contains("vượt quá giới hạn"),
                "Thông điệp phải nêu rõ dung lượng tệp vượt quá giới hạn. Thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("Từ chối tệp rỗng (0 bytes)")
    void testStoreEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "imageFile", "empty.jpg", "image/jpeg", new byte[0]
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> fileStorageService.storeProductImage(emptyFile),
                "Phải ném BadRequestException khi tệp rỗng");
        assertTrue(ex.getMessage().contains("không được để trống"),
                "Thông điệp phải thông báo tệp không được để trống. Thực tế: " + ex.getMessage());
    }

    @Test
    @DisplayName("Chống Path Traversal: Tên gốc chứa '../' hoặc đường dẫn tuyệt đối bị vô hiệu hóa")
    void testPathTraversalOriginalFilename() {
        MockMultipartFile traversalFile = new MockMultipartFile(
                "imageFile", "../../../../../Windows/System32/evil.jpg", "image/jpeg", VALID_JPG_BYTES
        );

        String publicUrl = fileStorageService.storeProductImage(traversalFile);

        assertNotNull(publicUrl, "Đường dẫn không được null");
        assertFalse(publicUrl.contains(".."), "Đường dẫn công khai không được chứa '..'. Thực tế: " + publicUrl);
        assertTrue(publicUrl.startsWith("/uploads/products/"), "Đường dẫn phải nằm trong /uploads/products/. Thực tế: " + publicUrl);

        String fileName = publicUrl.substring("/uploads/products/".length());
        Path physicalFile = tempDir.resolve("products").resolve(fileName).normalize();
        assertTrue(physicalFile.startsWith(tempDir.resolve("products")),
                "Tệp lưu trữ phải nằm trọn trong thư mục products của tempDir: " + physicalFile);
        assertTrue(Files.exists(physicalFile), "Tệp vật lý phải được tạo đúng vị trí: " + physicalFile);
    }

    @Test
    @DisplayName("Tên tệp lưu trữ bắt buộc là định dạng UUID hợp lệ")
    void testStoredNameIsUuid() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "my-beautiful-shoe.png", "image/png", VALID_PNG_BYTES
        );

        String publicUrl = fileStorageService.storeProductImage(file);
        String fileName = publicUrl.substring("/uploads/products/".length());
        String uuidPart = fileName.substring(0, fileName.lastIndexOf('.'));

        assertDoesNotThrow(() -> UUID.fromString(uuidPart),
                "Tên tệp trước phần mở rộng phải là chuỗi UUID chuẩn. Thực tế: " + uuidPart);
    }

    @Test
    @DisplayName("Xóa tệp: Chỉ xóa tệp nằm trong thư mục uploads, bỏ qua URL ngoài và ảnh tĩnh /images/...")
    void testDeleteFileOnlyInUploadsDir() throws IOException {
        // 1. Tạo 1 tệp thật trong uploads/products/
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "to-be-deleted.jpg", "image/jpeg", VALID_JPG_BYTES
        );
        String publicUrl = fileStorageService.storeProductImage(file);
        String fileName = publicUrl.substring("/uploads/products/".length());
        Path physicalFile = tempDir.resolve("products").resolve(fileName);
        assertTrue(Files.exists(physicalFile), "Tệp phải tồn tại trước khi xóa: " + physicalFile);

        // Thực hiện xóa tệp upload hợp lệ
        fileStorageService.deleteFile(publicUrl);
        assertFalse(Files.exists(physicalFile), "Tệp phải bị xóa khỏi đĩa sau khi gọi deleteFile: " + physicalFile);

        // 2. Thử xóa URL ngoài (http://...) -> không bị lỗi, không đụng tệp nào
        assertDoesNotThrow(() -> fileStorageService.deleteFile("https://cdn.example.com/images/shoe.jpg"),
                "deleteFile với URL ngoài không được ném ngoại lệ");

        // 3. Thử xóa ảnh tĩnh (/images/...) -> không bị lỗi
        assertDoesNotThrow(() -> fileStorageService.deleteFile("/images/placeholder-shoe.svg"),
                "deleteFile với ảnh tĩnh /images/ không được ném ngoại lệ");

        // 4. Thử path traversal khi xóa (/uploads/../../secret.txt)
        Path outsideFile = tempDir.resolve("secret.txt");
        Files.writeString(outsideFile, "Secret content");
        assertTrue(Files.exists(outsideFile), "Tệp ngoài phải tồn tại: " + outsideFile);

        fileStorageService.deleteFile("/uploads/../secret.txt");
        assertTrue(Files.exists(outsideFile), "Tệp ngoài thư mục uploads không bao giờ được phép bị xóa: " + outsideFile);
    }
}

