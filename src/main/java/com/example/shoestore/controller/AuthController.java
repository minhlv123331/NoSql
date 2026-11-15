package com.example.shoestore.controller;

import com.example.shoestore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị trang đăng nhập
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không chính xác. Vui lòng thử lại!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất tài khoản thành công!");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }

        return "login";
    }

    /**
     * Hiển thị trang đăng ký
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    /**
     * Xử lý đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // Giữ lại dữ liệu đã nhập để user không phải gõ lại nếu lỗi
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("address", address);

        // 1. Kiểm tra trường bắt buộc
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ họ tên, email và mật khẩu!");
            return "register";
        }

        // 2. Kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            model.addAttribute("errorMessage", "Mật khẩu phải có độ dài tối thiểu 6 ký tự!");
            return "register";
        }

        // 3. Kiểm tra mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "register";
        }

        // 4. Kiểm tra email đã tồn tại chưa
        if (userService.existsByEmail(email)) {
            model.addAttribute("errorMessage", "Email này đã được sử dụng. Vui lòng chọn email khác!");
            return "register";
        }

        try {
            userService.registerUser(fullName, email, phone, address, password);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi tạo tài khoản: " + e.getMessage());
            return "register";
        }
    }
}
