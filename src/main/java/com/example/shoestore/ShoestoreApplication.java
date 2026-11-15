package com.example.shoestore;

import com.example.shoestore.model.Shoe;
import com.example.shoestore.model.ShoeVariant;
import com.example.shoestore.model.User;
import com.example.shoestore.repository.ShoeRepository;
import com.example.shoestore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class ShoestoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoestoreApplication.class, args);
    }

    /**
     * Khởi tạo 2 tài khoản mặc định (Admin và User test) nếu chưa tồn tại
     */
    @Bean
    CommandLineRunner initUsers(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            // 1. Tạo tài khoản Admin
            if (!userRepo.existsByEmail("admin@gmail.com")) {
                Set<String> adminRoles = new HashSet<>(Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
                User admin = User.builder()
                        .fullName("Quản Trị Viên (Admin)")
                        .email("admin@gmail.com")
                        .password(encoder.encode("123456"))
                        .phone("0988888888")
                        .address("Hà Nội, Việt Nam")
                        .roles(adminRoles)
                        .build();
                userRepo.save(admin);
                System.out.println("✅ Đã tạo tài khoản Admin mẫu: admin@gmail.com / 123456");
            }

            // 2. Tạo tài khoản User test
            if (!userRepo.existsByEmail("user@gmail.com")) {
                Set<String> userRoles = new HashSet<>(List.of("ROLE_USER"));
                User user = User.builder()
                        .fullName("Nguyễn Văn Khách Hàng")
                        .email("user@gmail.com")
                        .password(encoder.encode("123456"))
                        .phone("0912345678")
                        .address("123 Nguyễn Huệ, TP. Hồ Chí Minh")
                        .roles(userRoles)
                        .build();
                userRepo.save(user);
                System.out.println("✅ Đã tạo tài khoản User mẫu: user@gmail.com / 123456");
            }
        };
    }

    /**
     * Tự động khởi tạo 12 sản phẩm giày mẫu đa dạng thương hiệu,
     * có đầy đủ variants (size 38-42, một số size stock = 0 để kiểm tra logic hết hàng).
     * Tự động làm sạch dữ liệu cũ nếu phát hiện dữ liệu thiếu trường variants.
     */
    @Bean
    CommandLineRunner initData(ShoeRepository repo) {
        return args -> {
            boolean hasInvalidData = false;
            try {
                List<Shoe> existing = repo.findAll();
                if (!existing.isEmpty()) {
                    hasInvalidData = existing.stream().anyMatch(s -> s.getVariants() == null || s.getVariants().isEmpty());
                }
            } catch (Exception e) {
                hasInvalidData = true;
            }

            if (repo.count() == 0 || hasInvalidData) {
                if (hasInvalidData) {
                    System.out.println("⚠️ Phát hiện dữ liệu cũ không tương thích cấu trúc variants mới. Đang làm mới dữ liệu...");
                    repo.deleteAll();
                }

                List<Shoe> sampleShoes = new ArrayList<>();

                // 1. Nike Air Force 1 '07
                sampleShoes.add(Shoe.builder()
                        .name("Nike Air Force 1 '07 Triple White")
                        .brand("Nike")
                        .category("Sneaker")
                        .price(2890000)
                        .imageUrl("https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800&auto=format&fit=crop&q=80")
                        .description("Thiết kế huyền thoại từ sân bóng rổ với chất liệu da cao cấp và đệm Air êm ái suốt ngày dài.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Trắng", 5),
                                new ShoeVariant("39", "Trắng", 8),
                                new ShoeVariant("40", "Trắng", 0), // hết hàng test
                                new ShoeVariant("41", "Trắng", 6),
                                new ShoeVariant("42", "Trắng", 4)
                        ))
                        .build());

                // 2. Nike Air Jordan 1 Low
                sampleShoes.add(Shoe.builder()
                        .name("Air Jordan 1 Low Chicago")
                        .brand("Nike")
                        .category("Basketball")
                        .price(3550000)
                        .imageUrl("https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?w=800&auto=format&fit=crop&q=80")
                        .description("Phiên bản cổ thấp của dòng giày biểu tượng mang phối màu đỏ - trắng kinh điển tôn vinh Michael Jordan.")
                        .variants(Arrays.asList(
                                new ShoeVariant("39", "Đỏ/Trắng", 3),
                                new ShoeVariant("40", "Đỏ/Trắng", 4),
                                new ShoeVariant("41", "Đỏ/Trắng", 0),
                                new ShoeVariant("42", "Đỏ/Trắng", 2)
                        ))
                        .build());

                // 3. Nike Pegasus 40
                sampleShoes.add(Shoe.builder()
                        .name("Nike Air Zoom Pegasus 40")
                        .brand("Nike")
                        .category("Running")
                        .price(3100000)
                        .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80")
                        .description("Chiến mã đường chạy hàng ngày với đệm React êm ái và hai túi Zoom Air trợ lực tối đa.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Đỏ Cam", 4),
                                new ShoeVariant("39", "Đỏ Cam", 7),
                                new ShoeVariant("40", "Đỏ Cam", 5),
                                new ShoeVariant("41", "Đỏ Cam", 3),
                                new ShoeVariant("42", "Đỏ Cam", 0)
                        ))
                        .build());

                // 4. Adidas Ultraboost Light
                sampleShoes.add(Shoe.builder()
                        .name("Adidas Ultraboost Light 23")
                        .brand("Adidas")
                        .category("Running")
                        .price(3800000)
                        .imageUrl("https://images.unsplash.com/photo-1587563871167-1ee9c731aefb?w=800&auto=format&fit=crop&q=80")
                        .description("Phiên bản Ultraboost nhẹ hơn 30% với công nghệ hạt Boost nảy hoàn trả năng lượng tối ưu.")
                        .variants(Arrays.asList(
                                new ShoeVariant("39", "Xám Trắng", 6),
                                new ShoeVariant("40", "Xám Trắng", 8),
                                new ShoeVariant("41", "Xám Trắng", 5),
                                new ShoeVariant("42", "Xám Trắng", 3)
                        ))
                        .build());

                // 5. Adidas Samba OG
                sampleShoes.add(Shoe.builder()
                        .name("Adidas Originals Samba OG")
                        .brand("Adidas")
                        .category("Casual")
                        .price(2700000)
                        .imageUrl("https://images.unsplash.com/photo-1518002171953-a080ee817e1f?w=800&auto=format&fit=crop&q=80")
                        .description("Mẫu sneaker đường phố hot nhất hiện nay với phong cách cổ điển, mũi da lộn và đế cao su gum.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Đen/Trắng", 0),
                                new ShoeVariant("39", "Đen/Trắng", 4),
                                new ShoeVariant("40", "Đen/Trắng", 2),
                                new ShoeVariant("41", "Đen/Trắng", 0),
                                new ShoeVariant("42", "Đen/Trắng", 1)
                        ))
                        .build());

                // 6. Adidas Forum Low Classic
                sampleShoes.add(Shoe.builder()
                        .name("Adidas Forum Low Classic")
                        .brand("Adidas")
                        .category("Sneaker")
                        .price(2400000)
                        .imageUrl("https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=800&auto=format&fit=crop&q=80")
                        .description("Mẫu giày phong cách thập niên 80 phối màu retro thanh lịch, phù hợp cho mọi hoạt động hàng ngày.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Trắng Xanh", 5),
                                new ShoeVariant("39", "Trắng Xanh", 6),
                                new ShoeVariant("40", "Trắng Xanh", 7),
                                new ShoeVariant("41", "Trắng Xanh", 4)
                        ))
                        .build());

                // 7. Puma RS-X Efekt
                sampleShoes.add(Shoe.builder()
                        .name("Puma RS-X Efekt Turbo")
                        .brand("Puma")
                        .category("Sneaker")
                        .price(2650000)
                        .imageUrl("https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&auto=format&fit=crop&q=80")
                        .description("Phong cách chunky sneaker phá cách với nhiều lớp layer phối màu nổi bật cùng hệ thống Running System.")
                        .variants(Arrays.asList(
                                new ShoeVariant("39", "Xanh Neon", 3),
                                new ShoeVariant("40", "Xanh Neon", 5),
                                new ShoeVariant("41", "Xanh Neon", 2),
                                new ShoeVariant("42", "Xanh Neon", 0)
                        ))
                        .build());

                // 8. Puma Suede Classic XXI
                sampleShoes.add(Shoe.builder()
                        .name("Puma Suede Classic XXI")
                        .brand("Puma")
                        .category("Casual")
                        .price(1850000)
                        .imageUrl("https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800&auto=format&fit=crop&q=80")
                        .description("Chất liệu da lộn mềm mại kinh điển cùng dải sọc Formstrip trứ danh tạo nên vẻ đẹp vượt thời gian.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Đỏ Đô", 4),
                                new ShoeVariant("39", "Đỏ Đô", 0),
                                new ShoeVariant("40", "Đỏ Đô", 3),
                                new ShoeVariant("41", "Đỏ Đô", 5)
                        ))
                        .build());

                // 9. New Balance 550
                sampleShoes.add(Shoe.builder()
                        .name("New Balance 550 Vintage")
                        .brand("New Balance")
                        .category("Basketball")
                        .price(3250000)
                        .imageUrl("https://images.unsplash.com/photo-1539185441755-769473a23570?w=800&auto=format&fit=crop&q=80")
                        .description("Tái hiện kiểu dáng bóng rổ thập niên 90 với form dáng cứng cáp và phong cách đường phố hoài cổ.")
                        .variants(Arrays.asList(
                                new ShoeVariant("39", "Trắng Xanh Lá", 2),
                                new ShoeVariant("40", "Trắng Xanh Lá", 4),
                                new ShoeVariant("41", "Trắng Xanh Lá", 6),
                                new ShoeVariant("42", "Trắng Xanh Lá", 1)
                        ))
                        .build());

                // 10. New Balance 574 Core
                sampleShoes.add(Shoe.builder()
                        .name("New Balance 574 Core Grey")
                        .brand("New Balance")
                        .category("Casual")
                        .price(2100000)
                        .imageUrl("https://images.unsplash.com/photo-1551107696-a4b085a6d966?w=800&auto=format&fit=crop&q=80")
                        .description("Đôi giày đa dụng và êm ái nhất của New Balance với công nghệ đệm ENCAP hỗ trợ bàn chân tuyệt vời.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Xám", 6),
                                new ShoeVariant("39", "Xám", 5),
                                new ShoeVariant("40", "Xám", 0),
                                new ShoeVariant("41", "Xám", 4),
                                new ShoeVariant("42", "Xám", 2)
                        ))
                        .build());

                // 11. Biti's Hunter X LiteDash
                sampleShoes.add(Shoe.builder()
                        .name("Biti's Hunter X LiteDash")
                        .brand("Biti's")
                        .category("Running")
                        .price(1099000)
                        .imageUrl("https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=800&auto=format&fit=crop&q=80")
                        .description("Đế LiteFlex đàn hồi vượt trội, vải dệt kháng khuẩn thoáng khí cùng thiết kế chuẩn phom chân người Việt.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Đen", 10),
                                new ShoeVariant("39", "Đen", 12),
                                new ShoeVariant("40", "Đen", 8),
                                new ShoeVariant("41", "Đen", 7),
                                new ShoeVariant("42", "Đen", 5)
                        ))
                        .build());

                // 12. Converse Chuck Taylor All Star 70
                sampleShoes.add(Shoe.builder()
                        .name("Converse Chuck 70 Vintage Canvas")
                        .brand("Converse")
                        .category("Casual")
                        .price(2000000)
                        .imageUrl("https://images.unsplash.com/photo-1607522370275-f14206abe5d3?w=800&auto=format&fit=crop&q=80")
                        .description("Chất vải canvas dày dặn, đường chỉ khâu tỉ mỉ cùng đế cao su bóng bẩy chuẩn phong cách di sản Mỹ.")
                        .variants(Arrays.asList(
                                new ShoeVariant("38", "Đen", 5),
                                new ShoeVariant("39", "Đen", 6),
                                new ShoeVariant("40", "Đen", 0),
                                new ShoeVariant("41", "Đen", 4),
                                new ShoeVariant("42", "Đen", 0) // size 40 và 42 hết hàng
                        ))
                        .build());

                repo.saveAll(sampleShoes);
                System.out.println("✅ Đã khởi tạo thành công 12 sản phẩm giày mẫu với đầy đủ variants!");
            }
        };
    }
}
