package com.dahdt.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình Web MVC: ánh xạ phục vụ tệp tải lên (/uploads/**).
 * Tự động ánh xạ tới thư mục tệp vật lý trên đĩa và không cho phép duyệt/liệt kê thư mục.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = uploadPath.toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(3600);
    }
}

