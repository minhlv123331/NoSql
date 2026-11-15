package com.example.shoestore.controller;

import com.example.shoestore.model.Cart;
import com.example.shoestore.security.CustomUserDetails;
import com.example.shoestore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Xem thông tin giỏ hàng
     */
    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUserId(userDetails.getId());
        model.addAttribute("cart", cart);

        return "cart";
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("shoeId") String shoeId,
            @RequestParam("size") String size,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            cartService.addToCart(userDetails.getId(), shoeId, size, color, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/shoes/" + shoeId;
        }

        return "redirect:/cart";
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @PostMapping("/update")
    public String updateQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("shoeId") String shoeId,
            @RequestParam("size") String size,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam("quantity") int quantity,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateItemQuantity(userDetails.getId(), shoeId, size, color, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Xóa sản phẩm khỏi giỏ
     */
    @PostMapping("/remove")
    public String removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("shoeId") String shoeId,
            @RequestParam("size") String size,
            @RequestParam(value = "color", required = false) String color,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeItem(userDetails.getId(), shoeId, size, color);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }
}
