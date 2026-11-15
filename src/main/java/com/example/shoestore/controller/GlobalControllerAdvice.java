package com.example.shoestore.controller;

import com.example.shoestore.security.CustomUserDetails;
import com.example.shoestore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    @Autowired
    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Tự động đưa tổng số lượng món hàng trong giỏ vào Model của mọi View
     */
    @ModelAttribute("cartItemCount")
    public int getCartItemCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getId() != null) {
            return cartService.getCartItemCount(userDetails.getId());
        }
        return 0;
    }
}
