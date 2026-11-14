package com.example.shoestore;

import com.example.shoestore.model.Shoe;
import com.example.shoestore.repository.ShoeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Arrays;

@SpringBootApplication
public class ShoestoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoestoreApplication.class, args);
    }

    // Tự động thêm 3 đôi giày mẫu vào MongoDB nếu database đang trống
    @Bean
    CommandLineRunner initData(ShoeRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Shoe(null, "Nike Air Force 1 '07", "Nike", 2500000, 
                    "https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/b7d9211c-26e7-431a-ac24-b0540fb3c00f/air-force-1-07-shoes-WrLlWX.png", 
                    Arrays.asList("39", "40", "41", "42")));
                repo.save(new Shoe(null, "Adidas Ultraboost Light", "Adidas", 3200000, 
                    "https://assets.adidas.com/images/w_600,f_auto,q_auto/0fbed4646c1d46e0aae0af6900d8f20c_9366/Giay_Ultraboost_Light_Trang_HQ6351_01_standard.jpg", 
                    Arrays.asList("40", "41", "43")));
                repo.save(new Shoe(null, "Biti's Hunter X LiteDash", "Biti's", 999000, 
                    "https://product.hstatic.net/1000230642/product/hsm004500den__2__6866c31c49a74957b2f81b3d018f087a_1024x1024.jpg", 
                    Arrays.asList("38", "39", "40", "41")));
                System.out.println("✅ Đã khởi tạo dữ liệu giày mẫu thành công!");
            }
        };
    }
}
