package com.example.shoestore.controller;

import com.example.shoestore.model.Cart;
import com.example.shoestore.model.Order;
import com.example.shoestore.security.CustomUserDetails;
import com.example.shoestore.service.CartService;
import com.example.shoestore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    /**
     * Màn hình Đặt hàng (Checkout)
     */
    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUserId(userDetails.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("user", userDetails.getUser());

        return "checkout";
    }

    /**
     * Xử lý xác nhận đặt hàng
     */
    @PostMapping("/checkout")
    public String processCheckout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("customerName") String customerName,
            @RequestParam("customerPhone") String customerPhone,
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam(value = "note", required = false) String note,
            @RequestParam(value = "paymentMethod", defaultValue = "COD") String paymentMethod,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.placeOrder(
                    userDetails.getId(),
                    customerName,
                    customerPhone,
                    shippingAddress,
                    note,
                    paymentMethod
            );
            return "redirect:/orders/" + order.getId() + "?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/checkout";
        }
    }

    /**
     * Danh sách lịch sử đơn hàng của người dùng
     */
    @GetMapping("/orders")
    public String listOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByUserId(userDetails.getId());
        model.addAttribute("orders", orders);

        return "orders";
    }

    /**
     * Xem chi tiết 1 đơn hàng cụ thể
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(
            @PathVariable("id") String id,
            @RequestParam(value = "success", required = false) Boolean success,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            Order order = orderService.getOrderDetailForUser(id, userDetails.getId());
            model.addAttribute("order", order);
            if (Boolean.TRUE.equals(success)) {
                model.addAttribute("successBanner", true);
            }
            return "order-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/orders";
        }
    }

    /**
     * Khách hàng tự hủy đơn hàng (khi đơn còn ở trạng thái PENDING)
     */
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            orderService.cancelOrderByUser(id, userDetails.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/orders/" + id;
    }
}
