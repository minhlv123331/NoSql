package com.example.shoestore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fullName;

    private String phone;

    private String address;

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    /**
     * Kiểm tra user có phải Admin hay không
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }
}
