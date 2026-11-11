package com.dahdt.ecommerce.config;

import com.dahdt.ecommerce.modules.order.entity.OrderStatus;
import com.dahdt.ecommerce.modules.payment.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Thành phần thuần Java lập kế hoạch dữ liệu đơn hàng demo (DemoDataPlanner).
 * Đảm bảo:
 * - Hạt giống ngẫu nhiên cố định (deterministic seed) để kết quả lặp lại được 100%.
 * - Đúng 25 đơn POS (thanh toán TIỀN MẶT, THẺ, QR xen kẽ, một số có voucher).
 * - Đúng 20 đơn ONLINE (COD và Ví điện tử xen kẽ, phân bố chính xác:
 *   6 PENDING, 3 CONFIRMED, 2 PACKING, 2 SHIPPING, 5 COMPLETED, 2 CANCELLED).
 * - Thời gian đặt đơn rải đều trong khoảng 14 ngày gần nhất.
 * - Hoàn toàn không phụ thuộc CSDL hay Spring Context để phục vụ kiểm thử đơn vị nhanh.
 */
public class DemoDataPlanner {

    public static final long DEFAULT_SEED = 20261111L;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemoPosOrderPlan {
        private int index;
        private String cashierEmail;
        private PaymentMethod paymentMethod;
        private String voucherCode;
        private BigDecimal cashReceived;
        private int variantIndex1;
        private int quantity1;
        private Integer variantIndex2;
        private Integer quantity2;
        private int daysAgo;
        private int hour;
        private int minute;
        private int second;
        private String note;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemoOnlineOrderPlan {
        private int index;
        private String customerEmail;
        private PaymentMethod paymentMethod;
        private String voucherCode;
        private OrderStatus targetStatus;
        private String shipName;
        private String shipPhone;
        private String shipAddress;
        private int variantIndex1;
        private int quantity1;
        private Integer variantIndex2;
        private Integer quantity2;
        private int daysAgo;
        private int hour;
        private int minute;
        private int second;
        private String note;
    }

    private final long seed;

    public DemoDataPlanner() {
        this(DEFAULT_SEED);
    }

    public DemoDataPlanner(long seed) {
        this.seed = seed;
    }

    /**
     * Lập kế hoạch đúng 25 đơn bán hàng tại quầy POS.
     */
    public List<DemoPosOrderPlan> buildPosPlans() {
        Random random = new Random(seed);
        List<DemoPosOrderPlan> plans = new ArrayList<>(25);

        for (int i = 0; i < 25; i++) {
            // Thanh toán TIỀN MẶT (CASH), THẺ (CARD), QR xen kẽ
            PaymentMethod method;
            int methodMod = i % 3;
            if (methodMod == 0) {
                method = PaymentMethod.CASH;
            } else if (methodMod == 1) {
                method = PaymentMethod.CARD;
            } else {
                method = PaymentMethod.QR;
            }

            // Voucher: áp dụng xen kẽ cho 6 đơn
            String voucherCode = null;
            if (i == 2 || i == 8 || i == 14) {
                voucherCode = "HELLOSUMMER";
            } else if (i == 5 || i == 11 || i == 20) {
                voucherCode = "GIAM50K";
            }

            // Tiền khách đưa khi thanh toán bằng tiền mặt
            BigDecimal cashReceived = (method == PaymentMethod.CASH)
                    ? BigDecimal.valueOf(3000000 + (long) (random.nextInt(3)) * 1000000)
                    : null;

            // Biến thể và số lượng
            int variantIdx1 = (i * 3 + 2) % 25;
            int qty1 = (i % 3 == 0) ? 2 : 1;

            Integer variantIdx2 = null;
            Integer qty2 = null;
            if (i % 4 == 0) {
                variantIdx2 = (i * 3 + 7) % 25;
                qty2 = 1;
            }

            // Rải đều 25 đơn trong 14 ngày (0..13 ngày trước)
            int daysAgo = (i * 14) / 25;
            int hour = 8 + random.nextInt(12); // 8h - 19h
            int minute = random.nextInt(60);
            int second = random.nextInt(60);

            plans.add(DemoPosOrderPlan.builder()
                    .index(i + 1)
                    .cashierEmail("cashier@dahdt.com")
                    .paymentMethod(method)
                    .voucherCode(voucherCode)
                    .cashReceived(cashReceived)
                    .variantIndex1(variantIdx1)
                    .quantity1(qty1)
                    .variantIndex2(variantIdx2)
                    .quantity2(qty2)
                    .daysAgo(daysAgo)
                    .hour(hour)
                    .minute(minute)
                    .second(second)
                    .note("[DEMO] Bán lẻ tại quầy POS")
                    .build());
        }

        return plans;
    }

    /**
     * Lập kế hoạch đúng 20 đơn đặt hàng trực tuyến (Shop Online).
     * Phân bố trạng thái: 6 PENDING, 3 CONFIRMED, 2 PACKING, 2 SHIPPING, 5 COMPLETED, 2 CANCELLED.
     */
    public List<DemoOnlineOrderPlan> buildOnlinePlans() {
        // Khởi tạo Random với seed offset để độc lập với POS
        Random random = new Random(seed + 100L);
        List<DemoOnlineOrderPlan> plans = new ArrayList<>(20);

        // Danh sách trạng thái theo đúng yêu cầu đề bài
        OrderStatus[] statuses = new OrderStatus[]{
                OrderStatus.PENDING, OrderStatus.PENDING, OrderStatus.PENDING,
                OrderStatus.PENDING, OrderStatus.PENDING, OrderStatus.PENDING, // 6 PENDING
                OrderStatus.CONFIRMED, OrderStatus.CONFIRMED, OrderStatus.CONFIRMED, // 3 CONFIRMED
                OrderStatus.PACKING, OrderStatus.PACKING, // 2 PACKING
                OrderStatus.SHIPPING, OrderStatus.SHIPPING, // 2 SHIPPING
                OrderStatus.COMPLETED, OrderStatus.COMPLETED, OrderStatus.COMPLETED,
                OrderStatus.COMPLETED, OrderStatus.COMPLETED, // 5 COMPLETED
                OrderStatus.CANCELLED, OrderStatus.CANCELLED // 2 CANCELLED
        };

        // Danh sách địa chỉ nhận hàng thực tế tại TP.HCM
        String[] addresses = new String[]{
                "123 Nguyễn Trãi, Phường Bến Thành, Quận 1, TP.HCM",
                "45 Lê Duẩn, Phường Bến Nghé, Quận 1, TP.HCM",
                "89 Cách Mạng Tháng 8, Phường 7, Quận 3, TP.HCM",
                "210 Hai Bà Trưng, Phường Tân Định, Quận 1, TP.HCM",
                "158 Bạch Đằng, Phường 2, Quận Tân Bình, TP.HCM",
                "450 Điện Biên Phủ, Phường 25, Quận Bình Thạnh, TP.HCM",
                "72 Nguyễn Thị Minh Khai, Phường 6, Quận 3, TP.HCM",
                "304 Hoàng Văn Thụ, Phường 4, Quận Tân Bình, TP.HCM",
                "18 Nam Kỳ Khởi Nghĩa, Phường 8, Quận 3, TP.HCM",
                "502 Xô Viết Nghệ Tĩnh, Phường 25, Quận Bình Thạnh, TP.HCM"
        };

        for (int i = 0; i < 20; i++) {
            OrderStatus targetStatus = statuses[i];

            // 10 đơn cho customer@dahdt.com, 10 đơn cho customer2@dahdt.com
            String customerEmail = (i < 10) ? "customer@dahdt.com" : "customer2@dahdt.com";
            String shipName = (i < 10) ? "Khách hàng Thân thiết" : "Khách hàng Thử nghiệm 2";
            String shipPhone = (i < 10) ? "0901000004" : "0901000005";
            String shipAddress = addresses[i % addresses.length];

            // COD và Ví điện tử xen kẽ
            PaymentMethod method = (i % 2 == 0) ? PaymentMethod.COD : PaymentMethod.EWALLET_MOCK;

            // Voucher: áp dụng xen kẽ cho một số đơn
            String voucherCode = null;
            if (i == 1 || i == 7 || i == 13) {
                voucherCode = "HELLOSUMMER";
            } else if (i == 4 || i == 10 || i == 16) {
                voucherCode = "GIAM50K";
            }

            // Biến thể và số lượng
            int variantIdx1 = (i * 4 + 5) % 25;
            int qty1 = (i % 3 == 0) ? 2 : 1;

            Integer variantIdx2 = null;
            Integer qty2 = null;
            if (i % 5 == 0) {
                variantIdx2 = (i * 4 + 9) % 25;
                qty2 = 1;
            }

            // Phân bố ngày đặt phù hợp với trạng thái:
            // PENDING mới đặt gần đây (0-3 ngày)
            // CONFIRMED, PACKING, SHIPPING giữa kỳ (2-7 ngày)
            // COMPLETED, CANCELLED trải dài hơn (4-13 ngày)
            int daysAgo;
            switch (targetStatus) {
                case PENDING -> daysAgo = i % 4; // 0, 1, 2, 3 ngày
                case CONFIRMED -> daysAgo = 2 + (i % 3); // 2, 3, 4 ngày
                case PACKING -> daysAgo = 3 + (i % 2); // 3, 4 ngày
                case SHIPPING -> daysAgo = 4 + (i % 2); // 4, 5 ngày
                case COMPLETED -> daysAgo = 6 + (i % 8); // 6..13 ngày
                case CANCELLED -> daysAgo = 5 + (i % 7); // 5..11 ngày
                default -> daysAgo = i % 14;
            }

            int hour = 9 + random.nextInt(12); // 9h - 20h
            int minute = random.nextInt(60);
            int second = random.nextInt(60);

            plans.add(DemoOnlineOrderPlan.builder()
                    .index(i + 1)
                    .customerEmail(customerEmail)
                    .paymentMethod(method)
                    .voucherCode(voucherCode)
                    .targetStatus(targetStatus)
                    .shipName(shipName)
                    .shipPhone(shipPhone)
                    .shipAddress(shipAddress)
                    .variantIndex1(variantIdx1)
                    .quantity1(qty1)
                    .variantIndex2(variantIdx2)
                    .quantity2(qty2)
                    .daysAgo(daysAgo)
                    .hour(hour)
                    .minute(minute)
                    .second(second)
                    .note("[DEMO] Đặt hàng trực tuyến")
                    .build());
        }

        return plans;
    }
}

