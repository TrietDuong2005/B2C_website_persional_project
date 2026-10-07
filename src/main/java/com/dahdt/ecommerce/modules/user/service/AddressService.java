package com.dahdt.ecommerce.modules.user.service;

import com.dahdt.ecommerce.common.exception.ResourceNotFoundException;
import com.dahdt.ecommerce.modules.user.dto.AddressRequestDTO;
import com.dahdt.ecommerce.modules.user.dto.AddressResponseDTO;
import com.dahdt.ecommerce.modules.user.entity.Address;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.mapper.AddressMapper;
import com.dahdt.ecommerce.modules.user.repository.AddressRepository;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    /**
     * Lấy danh sách địa chỉ của mình
     */
    public List<AddressResponseDTO> getMyAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Thêm mới địa chỉ
     */
    @Transactional
    public AddressResponseDTO createAddress(Long userId, AddressRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        // Nếu là địa chỉ mặc định, reset các địa chỉ cũ
        if (request.isDefault()) {
            resetDefaultAddresses(userId);
        }

        Address address = addressMapper.toEntity(request);
        address.setUser(user);
        
        // Nếu user chưa có địa chỉ nào, ép buộc thành mặc định
        List<Address> existing = addressRepository.findByUserId(userId);
        if (existing.isEmpty()) {
            address.setDefault(true);
        }

        address = addressRepository.save(address);
        return addressMapper.toDto(address);
    }

    /**
     * Sửa địa chỉ
     */
    @Transactional
    public AddressResponseDTO updateAddress(Long userId, Long addressId, AddressRequestDTO request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));

        if (request.isDefault() && !address.isDefault()) {
            resetDefaultAddresses(userId);
        }

        addressMapper.updateEntityFromDto(request, address);
        return addressMapper.toDto(addressRepository.save(address));
    }

    /**
     * Xóa địa chỉ
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy địa chỉ"));
        
        addressRepository.delete(address);
    }

    /**
     * Hàm hỗ trợ: bỏ cờ mặc định của tất cả địa chỉ thuộc về user
     */
    private void resetDefaultAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address addr : addresses) {
            if (addr.isDefault()) {
                addr.setDefault(false);
                addressRepository.save(addr);
            }
        }
    }
}
