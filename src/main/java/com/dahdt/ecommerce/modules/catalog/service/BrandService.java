package com.dahdt.ecommerce.modules.catalog.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.catalog.dto.BrandRequestDTO;
import com.dahdt.ecommerce.modules.catalog.dto.BrandResponseDTO;
import com.dahdt.ecommerce.modules.catalog.entity.Brand;
import com.dahdt.ecommerce.modules.catalog.mapper.BrandMapper;
import com.dahdt.ecommerce.modules.catalog.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public List<BrandResponseDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BrandResponseDTO createBrand(BrandRequestDTO request) {
        Brand brand = brandMapper.toEntity(request);
        return brandMapper.toDto(brandRepository.save(brand));
    }

    @Transactional
    public BrandResponseDTO updateBrand(Long id, BrandRequestDTO request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));
        brandMapper.updateEntityFromDto(request, brand);
        return brandMapper.toDto(brandRepository.save(brand));
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thương hiệu không tồn tại"));
        brandRepository.delete(brand);
    }
}
