package com.example.shoestore.service;

import com.example.shoestore.model.Cart;
import com.example.shoestore.model.CartItem;
import com.example.shoestore.model.Order;
import com.example.shoestore.model.OrderItem;
import com.example.shoestore.model.OrderStatus;
import com.example.shoestore.model.Shoe;
import com.example.shoestore.model.ShoeVariant;
import com.example.shoestore.repository.OrderRepository;
import com.example.shoestore.repository.ShoeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ShoeRepository shoeRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService, ShoeRepository shoeRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.shoeRepository = shoeRepository;
    }

    /**
     * Tiến hành đặt hàng: Kiểm tra tồn kho toàn bộ giỏ -> Trừ kho -> Tạo Đơn hàng -> Xóa giỏ hàng
     */
    public Order placeOrder(String userId, String customerName, String customerPhone,
                           String shippingAddress, String note, String paymentMethod) {

        if (customerName == null || customerName.trim().isEmpty() ||
            customerPhone == null || customerPhone.trim().isEmpty() ||
            shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng điền đầy đủ họ tên, số điện thoại và địa chỉ giao hàng!");
        }

        // 1. Lấy giỏ hàng
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng của bạn đang trống, không thể tiến hành đặt hàng!");
        }

        // 2. Kiểm tra tồn kho trước cho toàn bộ sản phẩm (đảm bảo atomic logic không trừ dở dang nếu thiếu hàng)
        for (CartItem item : cart.getItems()) {
            Shoe shoe = shoeRepository.findById(item.getShoeId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm '" + item.getShoeName() + "' không còn tồn tại trên hệ thống!"));

            ShoeVariant variant = findMatchingVariant(shoe, item.getSize(), item.getColor());
            if (variant == null) {
                throw new IllegalArgumentException("Kích cỡ " + item.getSize() + " của mẫu giày '" + item.getShoeName() + "' hiện không còn khả dụng!");
            }

            if (variant.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm '" + item.getShoeName() + "' (Size: " + item.getSize() +
                        ") chỉ còn " + variant.getStock() + " đôi trong kho, không đủ để đặt " + item.getQuantity() + " đôi!");
            }
        }

        // 3. Tiến hành trừ tồn kho thực tế của từng sản phẩm
        for (CartItem item : cart.getItems()) {
            Shoe shoe = shoeRepository.findById(item.getShoeId()).get();
            ShoeVariant variant = findMatchingVariant(shoe, item.getSize(), item.getColor());
            if (variant != null) {
                variant.setStock(variant.getStock() - item.getQuantity());
                shoeRepository.save(shoe);
            }
        }

        // 4. Chuyển đổi danh sách CartItem sang OrderItem (snapshot giá và thông tin tại thời điểm đặt hàng)
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> OrderItem.builder()
                        .shoeId(ci.getShoeId())
                        .shoeName(ci.getShoeName())
                        .brand(ci.getBrand())
                        .imageUrl(ci.getImageUrl())
                        .size(ci.getSize())
                        .color(ci.getColor())
                        .price(ci.getPrice())
                        .quantity(ci.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // 5. Tạo mã đơn hàng thân thiện (VD: SP-A1B2C3D4)
        String orderCode = "SP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderCode(orderCode)
                .userId(userId)
                .customerName(customerName.trim())
                .customerPhone(customerPhone.trim())
                .shippingAddress(shippingAddress.trim())
                .note(note != null ? note.trim() : "")
                .paymentMethod(paymentMethod != null ? paymentMethod : "COD")
                .status(OrderStatus.PENDING)
                .items(orderItems)
                .totalAmount(cart.getTotalAmount())
                .createdAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 6. Xóa sạch giỏ hàng của user sau khi tạo đơn thành công
        cartService.clearCart(userId);

        return savedOrder;
    }

    /**
     * Lấy danh sách đơn hàng của một người dùng cụ thể
     */
    public List<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Lấy chi tiết đơn hàng của người dùng, kiểm tra đúng quyền sở hữu
     */
    public Order getOrderDetailForUser(String orderId, String userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn hàng này!"));
    }

    /**
     * Lấy chi tiết đơn hàng theo ID (dành cho Admin)
     */
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã ID: " + orderId));
    }

    /**
     * Người dùng tự hủy đơn hàng (chỉ khi đơn hàng đang ở trạng thái PENDING)
     * Hoàn lại số lượng tồn kho cho các sản phẩm
     */
    public void cancelOrderByUser(String orderId, String userId) {
        Order order = getOrderDetailForUser(orderId, userId);

        if (!order.canCancel()) {
            throw new IllegalStateException("Đơn hàng hiện ở trạng thái '" + order.getStatus().getDisplayName() + "', không thể tự hủy!");
        }

        restoreStockForOrder(order);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    /**
     * Lấy toàn bộ đơn hàng (dành cho Admin)
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Lọc danh sách đơn hàng theo trạng thái (dành cho Admin)
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            return getAllOrders();
        }
        return orderRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Cập nhật trạng thái đơn hàng (dành cho Admin)
     */
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == newStatus) {
            return order;
        }

        // Không cho phép chỉnh sửa nếu đơn hàng đã giao thành công hoặc đã hủy
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể thay đổi trạng thái của đơn hàng đã kết thúc (" + order.getStatus().getDisplayName() + ")!");
        }

        // Nếu chuyển sang CANCELLED thì hoàn lại tồn kho
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStockForOrder(order);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Hoàn trả tồn kho cho các sản phẩm trong đơn khi bị hủy
     */
    private void restoreStockForOrder(Order order) {
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                shoeRepository.findById(item.getShoeId()).ifPresent(shoe -> {
                    ShoeVariant variant = findMatchingVariant(shoe, item.getSize(), item.getColor());
                    if (variant != null) {
                        variant.setStock(variant.getStock() + item.getQuantity());
                        shoeRepository.save(shoe);
                    }
                });
            }
        }
    }

    /**
     * Thống kê: Tổng doanh thu thực tế (đơn đã giao thành công)
     */
    public double getTotalDeliveredRevenue() {
        return orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.DELIVERED).stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    /**
     * Thống kê: Tổng doanh thu dự kiến (tất cả các đơn không bị hủy)
     */
    public double getExpectedRevenue() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    /**
     * Đếm tổng số đơn hàng trong hệ thống
     */
    public long countTotalOrders() {
        return orderRepository.count();
    }

    /**
     * Đếm số đơn hàng theo trạng thái
     */
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Lấy N đơn hàng mới nhất
     */
    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private ShoeVariant findMatchingVariant(Shoe shoe, String size, String color) {
        if (shoe.getVariants() == null) {
            return null;
        }
        for (ShoeVariant v : shoe.getVariants()) {
            if (v.getSize() != null && v.getSize().equalsIgnoreCase(size)) {
                if (color == null || color.trim().isEmpty() || color.equalsIgnoreCase(v.getColor())) {
                    return v;
                }
            }
        }
        return null;
    }
}
