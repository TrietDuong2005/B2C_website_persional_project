package com.dahdt.ecommerce.modules.store.entity;

/**
 * Vị trí chức vụ của nhân viên tại chi nhánh:
 * - MANAGER: Quản lý cửa hàng (toàn quyền quản lý kho, nhân sự, doanh thu chi nhánh)
 * - CASHIER: Thu ngân quầy (thao tác bán hàng POS, mở/đóng ca)
 * - STAFF: Nhân viên bán hàng / sắp xếp kho
 */
public enum EmployeePosition {
    MANAGER,
    CASHIER,
    STAFF
}

