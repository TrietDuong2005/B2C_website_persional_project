package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.common.util.SlugUtil;
import com.dahdt.ecommerce.modules.catalog.dto.BrandRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Brand;
import com.dahdt.ecommerce.modules.catalog.mapper.BrandMapper;
import com.dahdt.ecommerce.modules.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ quản lý Thương hiệu sản phẩm.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public Page<BrandResponseDTO> getBrands(String keyword, Pageable pageable) {
        String query = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return brandRepository.searchBrands(query, pageable)
                .map(this::enrichDto);
    }

    @Override
    public List<BrandResponseDTO> getAllActiveBrands() {
        return brandRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BrandResponseDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        return enrichDto(brand);
    }

    @Override
    @Transactional
    public BrandResponseDTO createBrand(BrandRequestDTO dto) {
        if (brandRepository.existsByName(dto.getName().trim())) {
            throw new BadRequestException("Tên thương hiệu '" + dto.getName() + "' đã tồn tại");
        }

        Brand brand = brandMapper.toEntity(dto);
        brand.setName(dto.getName().trim());
        brand.setSlug(generateUniqueSlug(dto.getName(), null));

        Brand saved = brandRepository.save(brand);
        return enrichDto(saved);
    }

    @Override
    @Transactional
    public BrandResponseDTO updateBrand(Long id, BrandRequestDTO dto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        if (brandRepository.existsByNameAndIdNot(dto.getName().trim(), id)) {
            throw new BadRequestException("Tên thương hiệu '" + dto.getName() + "' đã tồn tại ở bản ghi khác");
        }

        brand.setName(dto.getName().trim());
        brand.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        brand.setSlug(generateUniqueSlug(dto.getName(), id));

        Brand updated = brandRepository.save(brand);
        return enrichDto(updated);
    }

    @Override
    @Transactional
    public void toggleBrandStatus(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        brand.setIsActive(!Boolean.TRUE.equals(brand.getIsActive()));
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        // Quy tắc: Xoá = ẩn (is_active = false), không xoá cứng
        brand.setIsActive(false);
        brandRepository.save(brand);
    }

    // =========================================================================
    // HÀM BỔ TRỢ
    // =========================================================================

    private BrandResponseDTO enrichDto(Brand brand) {
        BrandResponseDTO dto = brandMapper.toDto(brand);
        dto.setProductCount(brandRepository.countProductsByBrandId(brand.getId()));
        return dto;
    }

    private String generateUniqueSlug(String name, Long existingId) {
        String baseSlug = SlugUtil.toSlug(name);
        if (baseSlug.isBlank()) {
            baseSlug = "thuong-hieu";
        }
        String candidate = baseSlug;
        int count = 1;
        while (existingId == null
                ? brandRepository.existsBySlug(candidate)
                : brandRepository.existsBySlugAndIdNot(candidate, existingId)) {
            candidate = baseSlug + "-" + count++;
        }
        return candidate;
    }
}

