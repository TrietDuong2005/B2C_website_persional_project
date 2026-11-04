package com.dahdt.ecommerce.modules.store.web;

import com.dahdt.ecommerce.common.exception.BadRequestException;
import com.dahdt.ecommerce.modules.store.dto.EmployeeRequestDTO;
import com.dahdt.ecommerce.modules.store.dto.EmployeeResponseDTO;
import com.dahdt.ecommerce.modules.store.dto.EmployeeUpdateDTO;
import com.dahdt.ecommerce.modules.store.dto.StoreResponseDTO;
import com.dahdt.ecommerce.modules.store.entity.EmployeePosition;
import com.dahdt.ecommerce.modules.store.entity.EmployeeStatus;
import com.dahdt.ecommerce.modules.store.service.EmployeeService;
import com.dahdt.ecommerce.modules.store.service.StoreScopeService;
import com.dahdt.ecommerce.modules.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller phục vụ trang Quản lý Nhân sự theo chi nhánh (/admin/stores/{storeId}/employees).
 * Phân quyền: ADMIN có thể xem/quản lý mọi chi nhánh; STORE_MANAGER chỉ xem/quản lý chi nhánh của chính mình.
 */
@Controller
@RequestMapping("/admin/stores/{storeId}/employees")
@PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeePageController {

    private final EmployeeService employeeService;
    private final StoreService storeService;
    private final StoreScopeService storeScopeService;

    /**
     * Hiển thị danh sách nhân sự tại chi nhánh.
     */
    @GetMapping
    public String listEmployees(@PathVariable("storeId") Long storeId,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        // Kiểm tra quyền truy cập chi nhánh (STORE_MANAGER chỉ được truy cập cửa hàng mình)
        storeScopeService.validateStoreAccess(storeId);

        StoreResponseDTO store = storeService.getStoreById(storeId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EmployeeResponseDTO> employeePage = employeeService.getEmployeesByStore(storeId, keyword, pageRequest);

        model.addAttribute("store", store);
        model.addAttribute("employeePage", employeePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("allStores", storeService.getActiveStores());
        model.addAttribute("positions", EmployeePosition.values());

        if (!model.containsAttribute("employeeForm")) {
            model.addAttribute("employeeForm", EmployeeRequestDTO.builder().storeId(storeId).build());
        }

        return "admin/store/employee-list";
    }

    /**
     * Thêm mới nhân viên vào chi nhánh bằng email tài khoản người dùng đã có.
     */
    @PostMapping("/add")
    public String addEmployee(@PathVariable("storeId") Long storeId,
                              @Valid @ModelAttribute("employeeForm") EmployeeRequestDTO form,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        storeScopeService.validateStoreAccess(storeId);
        form.setStoreId(storeId);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu nhập không hợp lệ, vui lòng kiểm tra lại!");
            redirectAttributes.addFlashAttribute("employeeForm", form);
            return "redirect:/admin/stores/" + storeId + "/employees";
        }

        try {
            employeeService.addEmployee(form);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên [" + form.getEmail() + "] thành công!");
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("employeeForm", form);
        } catch (Exception e) {
            log.error("Lỗi khi thêm nhân viên: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("employeeForm", form);
        }

        return "redirect:/admin/stores/" + storeId + "/employees";
    }

    /**
     * Cập nhật vị trí chức vụ hoặc điều chuyển chi nhánh cho nhân viên.
     */
    @PostMapping("/{id}/update")
    public String updateEmployee(@PathVariable("storeId") Long storeId,
                                 @PathVariable("id") Long id,
                                 @RequestParam("position") EmployeePosition position,
                                 @RequestParam("targetStoreId") Long targetStoreId,
                                 @RequestParam(value = "status", required = false) EmployeeStatus status,
                                 RedirectAttributes redirectAttributes) {
        storeScopeService.validateStoreAccess(storeId);

        try {
            EmployeeUpdateDTO updateDTO = EmployeeUpdateDTO.builder()
                    .id(id)
                    .storeId(targetStoreId)
                    .position(position)
                    .status(status)
                    .build();
            employeeService.updateEmployee(updateDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ nhân viên thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật nhân viên: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/stores/" + storeId + "/employees";
    }

    /**
     * Khóa hoặc mở khóa trạng thái làm việc của nhân viên.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleEmployeeStatus(@PathVariable("storeId") Long storeId,
                                       @PathVariable("id") Long id,
                                       RedirectAttributes redirectAttributes) {
        storeScopeService.validateStoreAccess(storeId);

        try {
            EmployeeResponseDTO emp = employeeService.getEmployeeById(id);
            if (EmployeeStatus.ACTIVE.equals(emp.getStatus())) {
                employeeService.lockEmployee(id);
                redirectAttributes.addFlashAttribute("successMessage", "Đã khóa nhân viên [" + emp.getEmail() + "]!");
            } else {
                employeeService.unlockEmployee(id);
                redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa nhân viên [" + emp.getEmail() + "]!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/stores/" + storeId + "/employees";
    }
}

