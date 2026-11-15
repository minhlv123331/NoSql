package com.example.shoestore.service;

import com.example.shoestore.model.User;
import com.example.shoestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Đăng ký tài khoản người dùng thông thường với role ROLE_USER
     */
    public User registerUser(String fullName, String email, String phone, String address, String rawPassword) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email '" + cleanEmail + "' đã được sử dụng!");
        }

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");

        User user = User.builder()
                .fullName(fullName.trim())
                .email(cleanEmail)
                .phone(phone != null ? phone.trim() : null)
                .address(address != null ? address.trim() : null)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    /**
     * Tạo tài khoản quản trị viên với role ROLE_ADMIN và ROLE_USER
     */
    public User registerAdmin(String fullName, String email, String phone, String address, String rawPassword) {
        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email '" + cleanEmail + "' đã được sử dụng!");
        }

        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        roles.add("ROLE_ADMIN");

        User admin = User.builder()
                .fullName(fullName.trim())
                .email(cleanEmail)
                .phone(phone != null ? phone.trim() : null)
                .address(address != null ? address.trim() : null)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();

        return userRepository.save(admin);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
