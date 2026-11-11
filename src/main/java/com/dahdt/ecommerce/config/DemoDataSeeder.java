package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.config.security.CustomUserDetails;
import com.dahdt.ecommerce.modules.cart.service.CartService;
import com.dahdt.ecommerce.modules.catalog.entity.ProductVariant;
import com.dahdt.ecommerce.modules.catalog.repository.ProductVariantRepository;
import com.dahdt.ecommerce.modules.inventory.dto.InventoryImportRequestDTO;
import com.dahdt.ecommerce.modules.inventory.service.InventoryService;
import com.dahdt.ecommerce.modules.order.dto.OrderResponseDTO;
import com.dahdt.ecommerce.modules.order.dto.PlaceOrderCommand;
import com.dahdt.ecommerce.modules.order.entity.Order;
import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.order.repository.OrderRepository;
import com.dahdt.ecommerce.modules.order.service.OrderService;
import com.dahdt.ecommerce.modules.order.service.OrderStatusService;
import com.dahdt.ecommerce.modules.payment.entity.Payment;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import com.dahdt.ecommerce.modules.payment.entity.PaymentStatus;
import com.dahdt.ecommerce.modules.payment.repository.PaymentRepository;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleCommand;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleItemCommand;
import com.dahdt.ecommerce.modules.pos.dto.PosSaleResponseDTO;
import com.dahdt.ecommerce.modules.pos.service.PosSaleService;
import com.dahdt.ecommerce.modules.store.entity.Store;
import com.dahdt.ecommerce.modules.store.repository.StoreRepository;
import com.dahdt.ecommerce.modules.user.entity.User;
import com.dahdt.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Seeder nạp DỮ LIỆU DEMO phục vụ xem Dashboard và chụp ảnh báo cáo đồ án (DemoDataSeeder).
 * Ràng buộc:
 * - Chỉ chạy ở profile 'dev' và khi cấu hình app.demo-data.enabled = true.
 * - Chạy sau DevDataSeeder (@org.springframework.core.annotation.Order(2)).
 * - Tính Idempotent: Nếu đã có đơn nào có ghi chú bắt đầu bằng "[DEMO]" thì bỏ qua toàn bộ.
 * - Thao tác hoàn toàn qua các Service nghiệp vụ thật (InventoryService, PosSaleService, CartService,
 *   OrderService, OrderStatusService); KHÔNG viết SQL ghi trực tiếp, KHÔNG tạo migration.
 * - Nạp dữ liệu biến thể bằng JOIN FETCH tới Product để triệt tiêu hoàn toàn lỗi LazyInitializationException
 *   khi chạy ngoài phiên Hibernate Session.
 * - Toàn bộ quá trình nạp bọc trong khối try/catch an toàn, không bao giờ làm dừng ứng dụng Spring Boot.
 * - Đảm bảo dọn dẹp SecurityContext trong khối finally sau mỗi đơn hàng.
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
@org.springframework.core.annotation.Order(2)
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryService inventoryService;
    private final PosSaleService posSaleService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;

    /**
     * Nạp danh sách biến thể sản phẩm đang mở bán (ACTIVE của sản phẩm cha ACTIVE)
     * với JOIN FETCH Product để tránh LazyInitializationException ngoài Hibernate Session.
     */
    public List<ProductVariant> loadSellableVariants() {
        return productVariantRepository.findSellableVariants();
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            executeSeeding();
        } catch (Exception ex) {
            log.error("Lỗi nghiêm trọng khi khởi tạo dữ liệu demo: {}. Ứng dụng vẫn tiếp tục chạy bình thường.",
                    ex.getMessage(), ex);
        }
    }

    /**
     * Thực thi toàn bộ quy trình nạp dữ liệu demo 4 bước an toàn.
     */
    private void executeSeeding() {
        log.info("==================================================");
        log.info("       BẮT ĐẦU NẠP DỮ LIỆU DEMO (DEMO DATA SEEDER)      ");
        log.info("==================================================");

        // 1. Kiểm tra tính Idempotent: Nếu đã có đơn demo thì bỏ qua
        if (orderRepository.existsByNoteStartingWithDemo()) {
            log.info("Đã phát hiện đơn hàng demo có sẵn trên hệ thống (ghi chú bắt đầu bằng '[DEMO]'). Bỏ qua toàn bộ.");
            return;
        }

        // 2. Tải các tài khoản dev từ CSDL
        User admin = userRepository.findByEmail("admin@dahdt.com").orElse(null);
        User cashier = userRepository.findByEmail("cashier@dahdt.com").orElse(null);
        User customer = userRepository.findByEmail("customer@dahdt.com").orElse(null);
        User customer2 = userRepository.findByEmail("customer2@dahdt.com").orElse(null);

        if (admin == null || cashier == null || customer == null || customer2 == null) {
            log.warn("Chưa đủ 4 tài khoản dev cần thiết (admin, cashier, customer, customer2). Bỏ qua nạp dữ liệu demo.");
            return;
        }

        // 3. Tải các cửa hàng và kho tổng
        Store whTong = storeRepository.findByCode("WH-TONG").orElse(null);
        Store storeQ1 = storeRepository.findByCode("STORE-Q1").orElse(null);
        Store storeQ3 = storeRepository.findByCode("STORE-Q3").orElse(null);
        Store storeBt = storeRepository.findByCode("STORE-BT").orElse(null);

        if (whTong == null || storeQ1 == null) {
            log.warn("Không tìm thấy kho tổng WH-TONG hoặc cửa hàng STORE-Q1. Bỏ qua nạp dữ liệu demo.");
            return;
        }

        // 4. Lấy danh sách biến thể đang mở bán qua truy vấn JOIN FETCH an toàn (chống LazyInitializationException)
        List<ProductVariant> sellableVariants = loadSellableVariants();

        if (sellableVariants.isEmpty()) {
            log.warn("Không tìm thấy biến thể sản phẩm đang mở bán nào. Bỏ qua nạp dữ liệu demo.");
            return;
        }

        // 5. Khởi tạo kế hoạch dữ liệu demo thuần Java
        DemoDataPlanner planner = new DemoDataPlanner();
        List<DemoDataPlanner.DemoPosOrderPlan> posPlans = planner.buildPosPlans();
        List<DemoDataPlanner.DemoOnlineOrderPlan> onlinePlans = planner.buildOnlinePlans();

        // Thu thập các biến thể tham gia vào đơn hàng demo
        Set<ProductVariant> variantsUsed = new HashSet<>();
        for (DemoDataPlanner.DemoPosOrderPlan p : posPlans) {
            variantsUsed.add(sellableVariants.get(p.getVariantIndex1() % sellableVariants.size()));
            if (p.getVariantIndex2() != null) {
                variantsUsed.add(sellableVariants.get(p.getVariantIndex2() % sellableVariants.size()));
            }
        }
        for (DemoDataPlanner.DemoOnlineOrderPlan p : onlinePlans) {
            variantsUsed.add(sellableVariants.get(p.getVariantIndex1() % sellableVariants.size()));
            if (p.getVariantIndex2() != null) {
                variantsUsed.add(sellableVariants.get(p.getVariantIndex2() % sellableVariants.size()));
            }
        }

        // =====================================================================
        // BƯỚC (a): NHẬP KHO BỔ SUNG AN TOÀN QUA INVENTORY SERVICE
        // =====================================================================
        log.info("Bước (a): Bổ sung tồn kho an toàn cho {} biến thể mẫu...", variantsUsed.size());
        List<Store> targetStores = new ArrayList<>();
        targetStores.add(whTong);
        targetStores.add(storeQ1);
        if (storeQ3 != null) targetStores.add(storeQ3);
        if (storeBt != null) targetStores.add(storeBt);

        for (ProductVariant v : variantsUsed) {
            for (Store s : targetStores) {
                try {
                    inventoryService.importStock(
                            InventoryImportRequestDTO.builder()
                                    .storeId(s.getId())
                                    .variantId(v.getId())
                                    .quantity(50) // Nhập thêm 50 đôi
                                    .note("[DEMO] Nhập kho mẫu")
                                    .build(),
                            admin.getId()
                    );
                } catch (Exception ex) {
                    log.warn("Không thể nhập kho mẫu cho SKU [{}] tại chi nhánh [{}]: {}", v.getSku(), s.getCode(), ex.getMessage());
                }
            }
        }
        log.info("Hoàn thành Bước (a): Đã nhập kho mẫu cho toàn bộ chi nhánh.");

        int posSuccess = 0;
        int onlineSuccess = 0;
        int errorCount = 0;
        Map<OrderStatus, Integer> statusDistribution = new EnumMap<>(OrderStatus.class);
        for (OrderStatus st : OrderStatus.values()) {
            statusDistribution.put(st, 0);
        }

        // =====================================================================
        // BƯỚC (b): TẠO 25 ĐƠN POS BẰNG POS SALE SERVICE
        // =====================================================================
        log.info("Bước (b): Bắt đầu tạo {} đơn bán lẻ tại quầy POS...", posPlans.size());
        for (DemoDataPlanner.DemoPosOrderPlan plan : posPlans) {
            try {
                // Thiết lập SecurityContext của thu ngân
                CustomUserDetails cashierDetails = new CustomUserDetails(cashier);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        cashierDetails, null, cashierDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                ProductVariant v1 = sellableVariants.get(plan.getVariantIndex1() % sellableVariants.size());
                List<PosSaleItemCommand> items = new ArrayList<>();
                items.add(new PosSaleItemCommand(v1.getId(), plan.getQuantity1()));

                if (plan.getVariantIndex2() != null) {
                    ProductVariant v2 = sellableVariants.get(plan.getVariantIndex2() % sellableVariants.size());
                    items.add(new PosSaleItemCommand(v2.getId(), plan.getQuantity2()));
                }

                PosSaleCommand cmd = PosSaleCommand.builder()
                        .items(items)
                        .voucherCode(plan.getVoucherCode())
                        .paymentMethod(plan.getPaymentMethod())
                        .cashReceived(plan.getCashReceived())
                        .build();

                PosSaleResponseDTO posResult = posSaleService.checkout(cashier.getId(), cmd);

                // Bước (d): Rải thời gian ngẫu nhiên nhưng lặp lại được
                LocalDateTime placedAt = calculateTargetDateTime(plan.getDaysAgo(), plan.getHour(), plan.getMinute(), plan.getSecond());

                Order order = orderRepository.findById(posResult.getOrderId()).orElse(null);
                if (order != null) {
                    order.setPlacedAt(placedAt);
                    order.setCompletedAt(placedAt);
                    order.setNote("[DEMO] Bán lẻ tại quầy POS");
                    orderRepository.save(order);

                    List<Payment> payments = paymentRepository.findByOrderId(order.getId());
                    for (Payment p : payments) {
                        p.setPaidAt(placedAt);
                        paymentRepository.save(p);
                    }
                }

                posSuccess++;
                statusDistribution.merge(OrderStatus.COMPLETED, 1, Integer::sum);
            } catch (Exception ex) {
                errorCount++;
                log.error("Lỗi khi tạo đơn POS demo số [{}]: {}", plan.getIndex(), ex.getMessage());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        log.info("Hoàn thành Bước (b): Tạo thành công {}/{} đơn POS.", posSuccess, posPlans.size());

        // =====================================================================
        // BƯỚC (c): TẠO 20 ĐƠN ONLINE BẰNG CART SERVICE + ORDER SERVICE + ORDER STATUS SERVICE
        // =====================================================================
        log.info("Bước (c): Bắt đầu tạo {} đơn đặt hàng trực tuyến...", onlinePlans.size());
        for (DemoDataPlanner.DemoOnlineOrderPlan plan : onlinePlans) {
            try {
                User currentCustomer = "customer@dahdt.com".equals(plan.getCustomerEmail()) ? customer : customer2;

                // 1. Đưa hàng vào giỏ và đặt đơn với SecurityContext của khách
                CustomUserDetails customerDetails = new CustomUserDetails(currentCustomer);
                Authentication customerAuth = new UsernamePasswordAuthenticationToken(
                        customerDetails, null, customerDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(customerAuth);

                cartService.clearCart(currentCustomer.getId());

                ProductVariant v1 = sellableVariants.get(plan.getVariantIndex1() % sellableVariants.size());
                cartService.addItem(currentCustomer.getId(), v1.getId(), plan.getQuantity1());

                if (plan.getVariantIndex2() != null) {
                    ProductVariant v2 = sellableVariants.get(plan.getVariantIndex2() % sellableVariants.size());
                    cartService.addItem(currentCustomer.getId(), v2.getId(), plan.getQuantity2());
                }

                PlaceOrderCommand cmd = PlaceOrderCommand.builder()
                        .shipName(plan.getShipName())
                        .shipPhone(plan.getShipPhone())
                        .shipAddress(plan.getShipAddress())
                        .paymentMethod(plan.getPaymentMethod())
                        .voucherCode(plan.getVoucherCode())
                        .note("[DEMO] Đặt hàng trực tuyến")
                        .build();

                OrderResponseDTO orderResponse = orderService.placeOrder(currentCustomer.getId(), cmd);
                Long orderId = orderResponse.getId();

                // 2. Chuyển trạng thái đơn hàng bằng OrderStatusService với SecurityContext của Admin
                CustomUserDetails adminDetails = new CustomUserDetails(admin);
                Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                        adminDetails, null, adminDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(adminAuth);

                OrderStatus targetStatus = plan.getTargetStatus();
                transitionOrderStatus(orderId, targetStatus, admin.getId());

                // Bước (d): Rải thời gian ngẫu nhiên lặp lại được
                LocalDateTime placedAt = calculateTargetDateTime(plan.getDaysAgo(), plan.getHour(), plan.getMinute(), plan.getSecond());
                LocalDateTime completedAt = (targetStatus == OrderStatus.COMPLETED)
                        ? placedAt.plusHours(12 + (plan.getIndex() % 24))
                        : null;

                if (completedAt != null && completedAt.isAfter(LocalDateTime.now())) {
                    completedAt = LocalDateTime.now().minusMinutes(2);
                }

                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null) {
                    order.setPlacedAt(placedAt);
                    order.setCompletedAt(completedAt);
                    order.setNote("[DEMO] Đặt hàng trực tuyến");
                    orderRepository.save(order);

                    List<Payment> payments = paymentRepository.findByOrderId(order.getId());
                    for (Payment p : payments) {
                        if (targetStatus == OrderStatus.COMPLETED) {
                            p.setPaidAt(p.getMethod() == PaymentMethod.COD ? completedAt : placedAt);
                        } else if (p.getStatus() == PaymentStatus.PAID || p.getStatus() == PaymentStatus.REFUNDED) {
                            p.setPaidAt(placedAt);
                        } else {
                            p.setPaidAt(null);
                        }
                        paymentRepository.save(p);
                    }
                }

                onlineSuccess++;
                statusDistribution.merge(targetStatus, 1, Integer::sum);
            } catch (Exception ex) {
                errorCount++;
                log.error("Lỗi khi tạo đơn Online demo số [{}]: {}", plan.getIndex(), ex.getMessage());
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
        log.info("Hoàn thành Bước (c): Tạo thành công {}/{} đơn Online.", onlineSuccess, onlinePlans.size());

        // =====================================================================
        // LOG TIẾNG VIỆT TỔNG KẾT
        // =====================================================================
        log.info("==================================================");
        log.info("      TỔNG KẾT NẠP DỮ LIỆU DEMO (DEMO DATA SEEDER)      ");
        log.info("==================================================");
        log.info("Tổng số đơn tạo thành công   : {}", (posSuccess + onlineSuccess));
        log.info(" - Đơn bán lẻ tại quầy (POS)  : {}", posSuccess);
        log.info(" - Đơn đặt trực tuyến (Online): {}", onlineSuccess);
        log.info("Phân bố đơn hàng theo trạng thái:");
        log.info(" - PENDING    (Chờ xử lý)     : {}", statusDistribution.get(OrderStatus.PENDING));
        log.info(" - CONFIRMED  (Đã xác nhận)   : {}", statusDistribution.get(OrderStatus.CONFIRMED));
        log.info(" - PACKING    (Đang đóng gói) : {}", statusDistribution.get(OrderStatus.PACKING));
        log.info(" - SHIPPING   (Đang giao)     : {}", statusDistribution.get(OrderStatus.SHIPPING));
        log.info(" - COMPLETED  (Hoàn tất)      : {}", statusDistribution.get(OrderStatus.COMPLETED));
        log.info(" - CANCELLED  (Đã hủy)        : {}", statusDistribution.get(OrderStatus.CANCELLED));
        log.info("Số lỗi phát sinh              : {}", errorCount);
        log.info("==================================================");
    }

    /**
     * Chuyển trạng thái đơn hàng theo đúng vòng đời nghiệp vụ (State Pattern).
     */
    private void transitionOrderStatus(Long orderId, OrderStatus targetStatus, Long adminUserId) {
        if (targetStatus == null || targetStatus == OrderStatus.PENDING) {
            return;
        }

        switch (targetStatus) {
            case CONFIRMED -> {
                orderStatusService.changeStatus(orderId, OrderStatus.CONFIRMED, adminUserId, "[DEMO] Quản trị viên duyệt đơn");
            }
            case PACKING -> {
                orderStatusService.changeStatus(orderId, OrderStatus.CONFIRMED, adminUserId, "[DEMO] Quản trị viên duyệt đơn");
                orderStatusService.changeStatus(orderId, OrderStatus.PACKING, adminUserId, "[DEMO] Đang đóng gói sản phẩm");
            }
            case SHIPPING -> {
                orderStatusService.changeStatus(orderId, OrderStatus.CONFIRMED, adminUserId, "[DEMO] Quản trị viên duyệt đơn");
                orderStatusService.changeStatus(orderId, OrderStatus.PACKING, adminUserId, "[DEMO] Đang đóng gói sản phẩm");
                orderStatusService.changeStatus(orderId, OrderStatus.SHIPPING, adminUserId, "[DEMO] Bàn giao đơn vị vận chuyển");
            }
            case COMPLETED -> {
                orderStatusService.changeStatus(orderId, OrderStatus.CONFIRMED, adminUserId, "[DEMO] Quản trị viên duyệt đơn");
                orderStatusService.changeStatus(orderId, OrderStatus.PACKING, adminUserId, "[DEMO] Đang đóng gói sản phẩm");
                orderStatusService.changeStatus(orderId, OrderStatus.SHIPPING, adminUserId, "[DEMO] Bàn giao đơn vị vận chuyển");
                orderStatusService.changeStatus(orderId, OrderStatus.COMPLETED, adminUserId, "[DEMO] Giao hàng thành công");
            }
            case CANCELLED -> {
                orderStatusService.changeStatus(orderId, OrderStatus.CANCELLED, adminUserId, "[DEMO] Khách hàng yêu cầu hủy đơn");
            }
            default -> {
                // Không hỗ trợ trạng thái khác
            }
        }
    }

    /**
     * Tính toán thời điểm đặt hàng trong 14 ngày gần nhất, đảm bảo không vượt quá thời gian hiện tại.
     */
    private LocalDateTime calculateTargetDateTime(int daysAgo, int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.minusDays(daysAgo)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(second);

        if (target.isAfter(now)) {
            target = now.minusMinutes(5);
        }
        return target;
    }
}
