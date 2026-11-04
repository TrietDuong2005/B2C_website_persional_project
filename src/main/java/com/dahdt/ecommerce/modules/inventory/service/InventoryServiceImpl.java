package com.dahdt.ecommerce.modules.inventory.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.dto.*;
import com.dahdt.ecommerce.modules.inventory.entity.Inventory;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransaction;
import com.dahdt.ecommerce.modules.inventory.entity.InventoryTransactionType;
import com.dahdt.ecommerce.modules.inventory.mapper.InventoryMapper;
import com.dahdt.ecommerce.modules.inventory.mapper.InventoryTransactionMapper;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryRepository;
import com.dahdt.ecommerce.modules.inventory.repository.InventoryTransactionRepository;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Cài đặt nghiệp vụ quản lý Tồn kho và Sổ cái giao dịch kho (InventoryService).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;

    @Override
    public Page<InventoryResponseDTO> getStoreInventory(Long storeId, String keyword, Long categoryId, String stockStatus, Pageable pageable) {
        String query = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String statusFilter = (stockStatus != null && !stockStatus.isBlank()) ? stockStatus.trim().toUpperCase() : null;

        Page<Inventory> inventoryPage = inventoryRepository.searchInventory(storeId, query, categoryId, statusFilter, pageable);
        return inventoryPage.map(inventoryMapper::toDto);
    }

    @Override
    public InventoryResponseDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng tồn kho với ID: " + id));
        return inventoryMapper.toDto(inventory);
    }

    @Override
    public InventoryResponseDTO getInventoryByStoreAndVariant(Long storeId, Long variantId) {
        Inventory inventory = inventoryRepository.findByStoreIdAndVariantId(storeId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tồn kho cho biến thể ID [" + variantId + "] tại chi nhánh [" + storeId + "]"));
        return inventoryMapper.toDto(inventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO importStock(InventoryImportRequestDTO dto, Long userId) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BadRequestException("Số lượng nhập kho phải lớn hơn 0!");
        }

        Inventory inventory = getOrCreateInventory(dto.getStoreId(), dto.getVariantId());

        // 1. Cập nhật tồn kho trong Domain Entity
        inventory.increase(dto.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);

        // 2. Ghi một dòng vào sổ cái giao dịch kho (Audit Log)
        User currentUser = (userId != null) ? userRepository.findById(userId).orElse(null) : null;
        InventoryTransaction tx = InventoryTransaction.builder()
                .variant(savedInventory.getVariant())
                .store(savedInventory.getStore())
                .type(InventoryTransactionType.IMPORT)
                .quantityChange(dto.getQuantity())
                .quantityAfter(savedInventory.getQuantityOnHand())
                .referenceType("MANUAL")
                .referenceId(null)
                .note(dto.getNote() != null ? dto.getNote().trim() : "Nhập kho thủ công")
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        inventoryTransactionRepository.save(tx);

        log.info("Nhập kho thành công: SKU [{}], Cửa hàng [{}], Số lượng [+{}] -> Tồn sau: [{}]",
                savedInventory.getVariant().getSku(), savedInventory.getStore().getCode(),
                dto.getQuantity(), savedInventory.getQuantityOnHand());

        return inventoryMapper.toDto(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO adjustStock(InventoryAdjustRequestDTO dto, Long userId) {
        if (dto.getQuantityChange() == null || dto.getQuantityChange() == 0) {
            throw new BadRequestException("Số lượng điều chỉnh kho phải khác 0!");
        }

        if (dto.getNote() == null || dto.getNote().trim().isBlank()) {
            throw new BadRequestException("Lý do điều chỉnh kho là bắt buộc!");
        }

        Inventory inventory = getOrCreateInventory(dto.getStoreId(), dto.getVariantId());

        // 1. Cập nhật tồn kho theo dấu của quantityChange (+ tăng, - giảm)
        int change = dto.getQuantityChange();
        if (change > 0) {
            inventory.increase(change);
        } else {
            inventory.deduct(-change); // ném InsufficientStockException nếu trừ quá số tồn
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        // 2. Ghi một dòng vào sổ cái giao dịch kho
        User currentUser = (userId != null) ? userRepository.findById(userId).orElse(null) : null;
        InventoryTransaction tx = InventoryTransaction.builder()
                .variant(savedInventory.getVariant())
                .store(savedInventory.getStore())
                .type(InventoryTransactionType.ADJUST)
                .quantityChange(change)
                .quantityAfter(savedInventory.getQuantityOnHand())
                .referenceType("MANUAL")
                .referenceId(null)
                .note(dto.getNote().trim())
                .createdBy(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        inventoryTransactionRepository.save(tx);

        log.info("Điều chỉnh kho thành công: SKU [{}], Cửa hàng [{}], Biến động [{}] -> Tồn sau: [{}]",
                savedInventory.getVariant().getSku(), savedInventory.getStore().getCode(),
                change, savedInventory.getQuantityOnHand());

        return inventoryMapper.toDto(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponseDTO updateReorderLevel(InventoryReorderLevelRequestDTO dto) {
        if (dto.getReorderLevel() == null || dto.getReorderLevel() < 0) {
            throw new BadRequestException("Mức đặt lại hàng (reorder_level) phải lớn hơn hoặc bằng 0!");
        }

        Inventory inventory = inventoryRepository.findByIdWithDetails(dto.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng tồn kho với ID: " + dto.getInventoryId()));

        inventory.setReorderLevel(dto.getReorderLevel());
        Inventory saved = inventoryRepository.save(inventory);

        log.info("Cập nhật ngưỡng đặt lại hàng thành công cho SKU [{}] tại chi nhánh [{}]: {}",
                saved.getVariant().getSku(), saved.getStore().getCode(), dto.getReorderLevel());

        return inventoryMapper.toDto(saved);
    }

    @Override
    public Page<InventoryTransactionResponseDTO> getLedger(Long storeId, String sku, InventoryTransactionType type,
                                                          LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        String skuQuery = (sku != null && !sku.isBlank()) ? sku.trim() : null;
        Page<InventoryTransaction> txPage = inventoryTransactionRepository.searchTransactions(
                storeId, skuQuery, type, fromDate, toDate, pageable
        );
        return txPage.map(inventoryTransactionMapper::toDto);
    }

    /**
     * Lấy bản ghi tồn kho hiện có hoặc khởi tạo mới với tồn bằng 0 nếu chưa tồn tại.
     */
    private Inventory getOrCreateInventory(Long storeId, Long variantId) {
        return inventoryRepository.findByStoreIdAndVariantId(storeId, variantId)
                .orElseGet(() -> {
                    Store store = storeRepository.findById(storeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + storeId));
                    ProductVariant variant = productVariantRepository.findById(variantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + variantId));

                    Inventory newInventory = Inventory.builder()
                            .store(store)
                            .variant(variant)
                            .quantityOnHand(0)
                            .reorderLevel(5)
                            .version(0L)
                            .build();

                    return inventoryRepository.save(newInventory);
                });
    }
}

