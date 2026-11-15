package com.example.shoestore.controller;

import com.example.shoestore.model.Shoe;
import com.example.shoestore.service.ShoeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ShoeController {

    private final ShoeService shoeService;

    @Autowired
    public ShoeController(ShoeService shoeService) {
        this.shoeService = shoeService;
    }

    /**
     * Trang chủ hiển thị danh sách giày kèm bộ lọc đa tiêu chí và phân trang
     */
    @GetMapping({"/", "/shoes"})
    public String index(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "6") int pageSize,
            Model model) {

        // Giới hạn trang không âm
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, pageSize);

        Page<Shoe> shoePage = shoeService.filterShoes(keyword, brand, category, minPrice, maxPrice, size, pageable);

        // Đưa dữ liệu danh sách sản phẩm và phân trang vào Model
        model.addAttribute("shoePage", shoePage);
        model.addAttribute("shoes", shoePage.getContent());
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", shoePage.getTotalPages());
        model.addAttribute("totalElements", shoePage.getTotalElements());

        // Đưa các tiêu chí lọc danh mục vào Model để render UI
        model.addAttribute("brands", shoeService.getAllBrands());
        model.addAttribute("categories", shoeService.getAllCategories());
        model.addAttribute("allSizes", shoeService.getAllStandardSizes());

        // Giữ lại các giá trị lọc người dùng đã chọn
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedBrand", brand != null ? brand : "");
        model.addAttribute("selectedCategory", category != null ? category : "");
        model.addAttribute("selectedSize", size != null ? size : "");
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "index";
    }

    /**
     * Xem chi tiết sản phẩm giày theo ID
     */
    @GetMapping("/shoes/{id}")
    public String shoeDetail(@PathVariable("id") String id, Model model) {
        Optional<Shoe> shoeOptional = shoeService.findById(id);
        if (shoeOptional.isEmpty()) {
            return "redirect:/?error=not_found";
        }

        Shoe shoe = shoeOptional.get();
        model.addAttribute("shoe", shoe);
        model.addAttribute("variants", shoe.getVariants());
        model.addAttribute("availableSizes", shoe.getAvailableSizes());
        model.addAttribute("totalStock", shoe.getTotalStock());

        return "shoe-detail";
    }
}
