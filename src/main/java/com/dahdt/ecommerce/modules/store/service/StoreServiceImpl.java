package com.dahdt.ecommerce.modules.store.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.store.dto.StoreRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.StoreResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.entity.StoreStatus;
import com.dahdt.ecommerce.modules.store.entity.StoreType;
import com.dahdt.ecommerce.modules.store.mapper.StoreMapper;
import com.dahdt.ecommerce.modules.store.repository.EmployeeRepository;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ quản lý chi nhánh và kho bãi (StoreService).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreMapper storeMapper;

    @Override
    public Page<StoreResponseDTO> getStores(String keyword, StoreType type, StoreStatus status, Pageable pageable) {
        String query = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Store> storePage = storeRepository.searchStores(query, type, status, pageable);

        return storePage.map(this::enrichStoreDto);
    }

    @Override
    public StoreResponseDTO getStoreById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        return enrichStoreDto(store);
    }

    @Override
    public StoreResponseDTO getStoreByCode(String code) {
        Store store = storeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với mã code: " + code));
        return enrichStoreDto(store);
    }

    @Override
    public List<StoreResponseDTO> getActiveStores() {
        List<Store> stores = storeRepository.findByStatusOrderByNameAsc(StoreStatus.ACTIVE);
        return stores.stream()
                .map(this::enrichStoreDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StoreResponseDTO createStore(StoreRequestDTO dto) {
        String code = dto.getCode().trim().toUpperCase();
        if (storeRepository.existsByCode(code)) {
            throw new BadRequestException("Mã chi nhánh [" + code + "] đã tồn tại trong hệ thống!");
        }

        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        Store store = storeMapper.toEntity(dto);
        store.setCode(code);
        store.setName(dto.getName().trim());
        store.setStatus(dto.getStatus() != null ? dto.getStatus() : StoreStatus.ACTIVE);

        Store saved = storeRepository.save(store);
        log.info("Đã tạo mới chi nhánh [{}] - {}", saved.getCode(), saved.getName());
        return enrichStoreDto(saved);
    }

    @Override
    @Transactional
    public StoreResponseDTO updateStore(Long id, StoreRequestDTO dto) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        String code = dto.getCode().trim().toUpperCase();
        if (storeRepository.existsByCodeAndIdNot(code, id)) {
            throw new BadRequestException("Mã chi nhánh [" + code + "] đã được sử dụng bởi một chi nhánh khác!");
        }

        validateCoordinates(dto.getLatitude(), dto.getLongitude());

        store.setCode(code);
        store.setName(dto.getName().trim());
        store.setType(dto.getType());
        store.setProvince(dto.getProvince().trim());
        store.setDistrict(dto.getDistrict().trim());
        store.setWard(dto.getWard().trim());
        store.setDetailAddress(dto.getDetailAddress().trim());
        store.setLatitude(dto.getLatitude());
        store.setLongitude(dto.getLongitude());
        store.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        if (dto.getStatus() != null) {
            store.setStatus(dto.getStatus());
        }

        Store updated = storeRepository.save(store);
        log.info("Đã cập nhật chi nhánh [{}] - {}", updated.getCode(), updated.getName());
        return enrichStoreDto(updated);
    }

    @Override
    @Transactional
    public void deactivateStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        store.setStatus(StoreStatus.INACTIVE);
        storeRepository.save(store);
        log.info("Đã ngưng hoạt động chi nhánh [{}] - {}", store.getCode(), store.getName());
    }

    @Override
    @Transactional
    public void activateStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        store.setStatus(StoreStatus.ACTIVE);
        storeRepository.save(store);
        log.info("Đã kích hoạt lại chi nhánh [{}] - {}", store.getCode(), store.getName());
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude != null) {
            if (latitude.compareTo(new BigDecimal("-90.0")) < 0 || latitude.compareTo(new BigDecimal("90.0")) > 0) {
                throw new BadRequestException("Vĩ độ (Latitude) phải nằm trong khoảng từ -90 đến 90 độ!");
            }
        }
        if (longitude != null) {
            if (longitude.compareTo(new BigDecimal("-180.0")) < 0 || longitude.compareTo(new BigDecimal("180.0")) > 0) {
                throw new BadRequestException("Kinh độ (Longitude) phải nằm trong khoảng từ -180 đến 180 độ!");
            }
        }
    }

    private StoreResponseDTO enrichStoreDto(Store store) {
        StoreResponseDTO dto = storeMapper.toDto(store);
        long count = employeeRepository.countByStoreId(store.getId());
        dto.setEmployeeCount(count);
        return dto;
    }
}

