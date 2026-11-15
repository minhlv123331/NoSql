package com.example.shoestore.service;

import com.example.shoestore.model.Shoe;
import com.example.shoestore.model.ShoeVariant;
import com.example.shoestore.repository.ShoeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ShoeService {

    private final ShoeRepository shoeRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ShoeService(ShoeRepository shoeRepository, MongoTemplate mongoTemplate) {
        this.shoeRepository = shoeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Lấy danh sách tất cả sản phẩm
     */
    public List<Shoe> findAll() {
        return shoeRepository.findAll();
    }

    /**
     * Tìm sản phẩm theo ID
     */
    public Optional<Shoe> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return shoeRepository.findById(id);
    }

    /**
     * Lưu hoặc cập nhật sản phẩm
     */
    public Shoe save(Shoe shoe) {
        return shoeRepository.save(shoe);
    }

    /**
     * Xóa sản phẩm theo ID
     */
    public void deleteById(String id) {
        shoeRepository.deleteById(id);
    }

    /**
     * Tìm kiếm và lọc đa điều kiện kết hợp phân trang cho khách hàng
     */
    public Page<Shoe> filterShoes(String keyword, String brand, String category,
                                  Double minPrice, Double maxPrice, String size,
                                  Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            criteriaList.add(Criteria.where("name").regex(Pattern.quote(keyword.trim()), "i"));
        }

        if (brand != null && !brand.trim().isEmpty()) {
            criteriaList.add(Criteria.where("brand").is(brand.trim()));
        }

        if (category != null && !category.trim().isEmpty()) {
            criteriaList.add(Criteria.where("category").is(category.trim()));
        }

        if (minPrice != null && minPrice > 0 && maxPrice != null && maxPrice > 0) {
            criteriaList.add(Criteria.where("price").gte(minPrice).lte(maxPrice));
        } else if (minPrice != null && minPrice > 0) {
            criteriaList.add(Criteria.where("price").gte(minPrice));
        } else if (maxPrice != null && maxPrice > 0) {
            criteriaList.add(Criteria.where("price").lte(maxPrice));
        }

        if (size != null && !size.trim().isEmpty()) {
            criteriaList.add(Criteria.where("variants").elemMatch(
                    Criteria.where("size").is(size.trim()).and("stock").gt(0)
            ));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria);
        long total = mongoTemplate.count(query, Shoe.class);

        query.with(pageable);
        List<Shoe> content = mongoTemplate.find(query, Shoe.class);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Tìm kiếm sản phẩm cho Quản trị viên (Admin)
     */
    public Page<Shoe> searchShoesForAdmin(String keyword, Pageable pageable) {
        Query query = new Query();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String cleanKw = Pattern.quote(keyword.trim());
            Criteria orCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex(cleanKw, "i"),
                    Criteria.where("brand").regex(cleanKw, "i"),
                    Criteria.where("category").regex(cleanKw, "i")
            );
            query.addCriteria(orCriteria);
        }

        long total = mongoTemplate.count(query, Shoe.class);
        query.with(pageable);
        List<Shoe> list = mongoTemplate.find(query, Shoe.class);

        return new PageImpl<>(list, pageable, total);
    }

    /**
     * Lưu file tải lên vào thư mục vật lý uploads/
     */
    public String saveUploadedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg");
            String fileName = UUID.randomUUID().toString().substring(0, 8) + "_" + originalName;
            Path filePath = uploadDir.resolve(fileName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh tải lên: " + e.getMessage(), e);
        }
    }

    /**
     * Lưu thông tin giày và các biến thể từ Form Admin
     */
    public Shoe saveShoeFromAdmin(Shoe formShoe, MultipartFile imageFile,
                                  List<String> sizes, List<String> colors, List<Integer> stocks) {

        // 1. Xử lý ảnh: Ưu tiên file upload mới, nếu không upload mới thì giữ ảnh cũ / URL đã nhập
        if (imageFile != null && !imageFile.isEmpty()) {
            String uploadedUrl = saveUploadedFile(imageFile);
            formShoe.setImageUrl(uploadedUrl);
        } else if (formShoe.getId() != null && !formShoe.getId().isEmpty()) {
            // Đang sửa: nếu không chọn file mới và để trống imageUrl thì giữ nguyên ảnh cũ trong DB
            if (formShoe.getImageUrl() == null || formShoe.getImageUrl().trim().isEmpty()) {
                findById(formShoe.getId()).ifPresent(old -> formShoe.setImageUrl(old.getImageUrl()));
            }
        }

        // 2. Xử lý danh sách biến thể (Size/Màu/Tồn kho)
        List<ShoeVariant> variants = new ArrayList<>();
        if (sizes != null && !sizes.isEmpty()) {
            for (int i = 0; i < sizes.size(); i++) {
                String size = sizes.get(i);
                if (size != null && !size.trim().isEmpty()) {
                    String color = (colors != null && i < colors.size() && colors.get(i) != null) ? colors.get(i).trim() : "Mặc định";
                    int stock = (stocks != null && i < stocks.size() && stocks.get(i) != null) ? Math.max(0, stocks.get(i)) : 0;
                    variants.add(new ShoeVariant(size.trim(), color, stock));
                }
            }
        }
        formShoe.setVariants(variants);

        return shoeRepository.save(formShoe);
    }

    /**
     * Đếm tổng số lượng sản phẩm giày trong hệ thống
     */
    public long countTotalShoes() {
        return shoeRepository.count();
    }

    /**
     * Đếm số lượng sản phẩm hết hàng hoặc sắp hết hàng (tổng tồn kho <= 5)
     */
    public long countLowStockShoes() {
        return shoeRepository.findAll().stream()
                .filter(s -> s.getTotalStock() <= 5)
                .count();
    }

    /**
     * Lấy danh sách thương hiệu
     */
    public List<String> getAllBrands() {
        List<String> brands = mongoTemplate.findDistinct(new Query(), "brand", Shoe.class, String.class);
        if (brands.isEmpty()) {
            return Arrays.asList("Nike", "Adidas", "Puma", "Biti's", "New Balance", "Converse");
        }
        return brands;
    }

    /**
     * Lấy danh sách danh mục
     */
    public List<String> getAllCategories() {
        List<String> categories = mongoTemplate.findDistinct(new Query(), "category", Shoe.class, String.class);
        if (categories.isEmpty()) {
            return Arrays.asList("Running", "Sneaker", "Basketball", "Casual");
        }
        return categories;
    }

    public List<String> getAllStandardSizes() {
        return Arrays.asList("38", "39", "40", "41", "42");
    }
}
