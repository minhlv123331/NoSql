package com.example.shoestore.controller;

import com.example.shoestore.model.Shoe;
import com.example.shoestore.repository.ShoeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ShoeController {

    @Autowired
    private ShoeRepository shoeRepository;

    // Hiển thị trang chủ danh sách giày
    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("shoes", shoeRepository.findAll());
        model.addAttribute("newShoe", new Shoe());
        return "index";
    }

    // Xử lý thêm giày mới từ form trên web
    @PostMapping("/add-shoe")
    public String addShoe(@ModelAttribute Shoe newShoe) {
        shoeRepository.save(newShoe);
        return "redirect:/";
    }
}