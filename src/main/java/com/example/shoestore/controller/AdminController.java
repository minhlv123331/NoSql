package com.example.shoestore.controller;

import com.example.shoestore.model.Order;
import com.example.shoestore.model.OrderStatus;
import com.example.shoestore.model.Shoe;
import com.example.shoestore.model.ShoeVariant;
import com.example.shoestore.repository.UserRepository;
import com.example.shoestore.service.OrderService;
import com.example.shoestore.service.ShoeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ShoeService shoeService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @Autowired
    public AdminController(ShoeService shoeService, OrderService orderService, UserRepository userRepository) {
        this.shoeService = shoeService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * Dashboard tổng quan dành cho Quản trị viên
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        // 1. Thống kê doanh thu
        double deliveredRevenue = orderService.getTotalDeliveredRevenue();
        double expectedRevenue = orderService.getExpectedRevenue();

        // 2. Thống kê đơn hàng
        long totalOrders = orderService.countTotalOrders();
        long pendingOrders = orderService.countOrdersByStatus(OrderStatus.PENDING);

        // 3. Thống kê sản phẩm
        long totalShoes = shoeService.countTotalShoes();
        long lowStockShoes = shoeService.countLowStockShoes();

        // 4. Thống kê người dùng
        long totalUsers = userRepository.count();

        // 5. 5 đơn hàng mới nhất
        List<Order> recentOrders = orderService.getRecentOrders(5);

        model.addAttribute("deliveredRevenue", deliveredRevenue);
        model.addAttribute("expectedRevenue", expectedRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalShoes", totalShoes);
        model.addAttribute("lowStockShoes", lowStockShoes);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("recentOrders", recentOrders);

        return "admin/dashboard";
    }

    // ==========================================
    // QUẢN LÝ SẢN PHẨM (SHOES)
    // ==========================================

    /**
     * Danh sách sản phẩm (có tìm kiếm & phân trang)
     */
    @GetMapping("/shoes")
    public String listShoes(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            Model model) {

        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, pageSize);
        Page<Shoe> shoePage = shoeService.searchShoesForAdmin(keyword, pageable);

        model.addAttribute("shoePage", shoePage);
        model.addAttribute("shoes", shoePage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", shoePage.getTotalPages());
        model.addAttribute("totalElements", shoePage.getTotalElements());
        model.addAttribute("keyword", keyword != null ? keyword : "");

        return "admin/shoes";
    }

    /**
     * Mở form thêm mới sản phẩm
     */
    @GetMapping("/shoes/new")
    public String newShoeForm(Model model) {
        Shoe shoe = new Shoe();
        // Mặc định tạo sẵn 2 dòng biến thể mẫu để tiện nhập
        List<ShoeVariant> defaultVariants = new ArrayList<>();
        defaultVariants.add(new ShoeVariant("39", "Trắng", 10));
        defaultVariants.add(new ShoeVariant("40", "Trắng", 10));
        shoe.setVariants(defaultVariants);

        model.addAttribute("shoe", shoe);
        model.addAttribute("brands", shoeService.getAllBrands());
        model.addAttribute("categories", shoeService.getAllCategories());
        model.addAttribute("isNew", true);

        return "admin/shoe-form";
    }

    /**
     * Mở form chỉnh sửa sản phẩm
     */
    @GetMapping("/shoes/edit/{id}")
    public String editShoeForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Shoe> shoeOpt = shoeService.findById(id);
        if (shoeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm ID: " + id);
            return "redirect:/admin/shoes";
        }

        Shoe shoe = shoeOpt.get();
        if (shoe.getVariants() == null || shoe.getVariants().isEmpty()) {
            shoe.setVariants(new ArrayList<>(List.of(new ShoeVariant("40", "Mặc định", 5))));
        }

        model.addAttribute("shoe", shoe);
        model.addAttribute("brands", shoeService.getAllBrands());
        model.addAttribute("categories", shoeService.getAllCategories());
        model.addAttribute("isNew", false);

        return "admin/shoe-form";
    }

    /**
     * Lưu thông tin sản phẩm (thêm mới hoặc cập nhật)
     */
    @PostMapping("/shoes/save")
    public String saveShoe(
            @ModelAttribute Shoe shoe,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "variantSizes", required = false) List<String> sizes,
            @RequestParam(value = "variantColors", required = false) List<String> colors,
            @RequestParam(value = "variantStocks", required = false) List<Integer> stocks,
            RedirectAttributes redirectAttributes) {

        try {
            shoeService.saveShoeFromAdmin(shoe, imageFile, sizes, colors, stocks);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm '" + shoe.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu sản phẩm: " + e.getMessage());
        }

        return "redirect:/admin/shoes";
    }

    /**
     * Xóa sản phẩm
     */
    @PostMapping("/shoes/delete/{id}")
    public String deleteShoe(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            shoeService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/shoes";
    }

    // ==========================================
    // QUẢN LÝ ĐƠN HÀNG (ORDERS)
    // ==========================================

    /**
     * Danh sách tất cả đơn hàng (có bộ lọc theo trạng thái)
     */
    @GetMapping("/orders")
    public String listOrders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            Model model) {

        List<Order> orders = orderService.getOrdersByStatus(status);
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());

        return "admin/orders";
    }

    /**
     * Xem chi tiết đơn hàng phía Admin
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            model.addAttribute("order", order);
            model.addAttribute("allStatuses", OrderStatus.values());
            return "admin/order-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng từ Admin
     */
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable("id") String id,
            @RequestParam("status") OrderStatus newStatus,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.updateOrderStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng sang '" + newStatus.getDisplayName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/orders/" + id;
    }
}
