package com.dahdt.ecommerce.modules.pos.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.pos.dto.PosProductSearchResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Employee;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ tra cứu sản phẩm tại quầy POS.
 * Tìm biến thể ACTIVE của sản phẩm ACTIVE, lấy tồn tại cửa hàng thu ngân làm việc,
 * tối ưu hóa truy vấn chống N+1 query và giới hạn 20 dòng.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PosCatalogServiceImpl implements PosCatalogService {

    private final EmployeeRepository employeeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<PosProductSearchResponseDTO> search(Long userId, String keyword) {
        if (userId == null) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        // 1. Xác định nhân viên và cửa hàng làm việc
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Tài khoản không thuộc cửa hàng nào."));

        if (!employee.isActive()) {
            throw new BadRequestException("Tài khoản nhân viên đã bị khóa hoặc tạm dừng hoạt động.");
        }

        if (!employee.isCashier() && !employee.isManager()) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        Store store = employee.getStore();
        if (store == null) {
            throw new BadRequestException("Tài khoản không thuộc cửa hàng nào.");
        }

        if (keyword == null || keyword.trim().isBlank()) {
            return Collections.emptyList();
        }

        String searchKeyword = keyword.trim();

        // 2. Tìm kiếm tối đa 20 biến thể ACTIVE thuộc sản phẩm ACTIVE (Eager fetch product chống N+1)
        List<ProductVariant> variants = productVariantRepository.searchActiveVariantsForPos(
                searchKeyword,
                PageRequest.of(0, 20)
        );

        if (variants.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Lấy số lượng tồn kho tại CỬA HÀNG CỦA THU NGÂN trong 1 query duy nhất (chống N+1)
        List<Long> variantIds = variants.stream().map(ProductVariant::getId).toList();
        List<Inventory> inventories = inventoryRepository.findByStoreIdAndVariantIdIn(store.getId(), variantIds);

        Map<Long, Integer> stockMap = inventories.stream()
                .collect(Collectors.toMap(
                        inv -> inv.getVariant().getId(),
                        inv -> (inv.getQuantityOnHand() != null) ? inv.getQuantityOnHand() : 0,
                        (k1, k2) -> k1
                ));

        // 4. Ánh xạ sang DTO trả về
        return variants.stream()
                .map(variant -> PosProductSearchResponseDTO.builder()
                        .variantId(variant.getId())
                        .sku(variant.getSku())
                        .barcode(variant.getBarcode())
                        .productName(variant.getProduct() != null ? variant.getProduct().getName() : "")
                        .color(variant.getColor())
                        .size(variant.getSize())
                        .unitPrice(variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO)
                        .quantityOnHand(stockMap.getOrDefault(variant.getId(), 0))
                        .build())
                .toList();
    }
}

