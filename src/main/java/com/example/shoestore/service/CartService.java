package com.example.shoestore.service;

import com.example.shoestore.model.Cart;
import com.example.shoestore.model.CartItem;
import com.example.shoestore.model.Shoe;
import com.example.shoestore.model.ShoeVariant;
import com.example.shoestore.repository.CartRepository;
import com.example.shoestore.repository.ShoeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ShoeRepository shoeRepository;

    @Autowired
    public CartService(CartRepository cartRepository, ShoeRepository shoeRepository) {
        this.cartRepository = cartRepository;
        this.shoeRepository = shoeRepository;
    }

    /**
     * Lấy giỏ hàng của người dùng, nếu chưa có thì khởi tạo giỏ hàng mới
     */
    public Cart getCartByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return new Cart(null, null, new ArrayList<>());
        }
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    /**
     * Lấy tổng số lượng item trong giỏ của user (dùng cho badge giỏ hàng trên Navbar)
     */
    public int getCartItemCount(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0;
        }
        return cartRepository.findByUserId(userId)
                .map(Cart::getTotalItems)
                .orElse(0);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public void addToCart(String userId, String shoeId, String size, String color, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        // 1. Kiểm tra sản phẩm tồn tại
        Shoe shoe = shoeRepository.findById(shoeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm giày!"));

        // 2. Tìm biến thể size & màu tương ứng
        ShoeVariant targetVariant = null;
        if (shoe.getVariants() != null) {
            for (ShoeVariant v : shoe.getVariants()) {
                if (v.getSize() != null && v.getSize().equalsIgnoreCase(size)) {
                    if (color == null || color.trim().isEmpty() || color.equalsIgnoreCase(v.getColor())) {
                        targetVariant = v;
                        break;
                    }
                }
            }
        }

        if (targetVariant == null) {
            throw new IllegalArgumentException("Không tìm thấy kích cỡ '" + size + "' cho sản phẩm này!");
        }

        if (targetVariant.getStock() <= 0) {
            throw new IllegalArgumentException("Kích cỡ " + size + " hiện tại đã hết hàng!");
        }

        // 3. Lấy hoặc tạo giỏ hàng
        Cart cart = getCartByUserId(userId);
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        final String finalColor = targetVariant.getColor() != null ? targetVariant.getColor() : "";
        final String finalSize = targetVariant.getSize();

        // 4. Kiểm tra xem đã có món hàng này trong giỏ chưa
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getShoeId().equals(shoeId)
                        && i.getSize().equalsIgnoreCase(finalSize)
                        && (finalColor.isEmpty() || finalColor.equalsIgnoreCase(i.getColor())))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQty = item.getQuantity() + quantity;
            if (newQty > targetVariant.getStock()) {
                newQty = targetVariant.getStock();
            }
            item.setQuantity(newQty);
        } else {
            int initialQty = Math.min(quantity, targetVariant.getStock());
            CartItem newItem = CartItem.builder()
                    .shoeId(shoe.getId())
                    .shoeName(shoe.getName())
                    .brand(shoe.getBrand())
                    .imageUrl(shoe.getImageUrl())
                    .size(finalSize)
                    .color(finalColor)
                    .price(shoe.getPrice())
                    .quantity(initialQty)
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    /**
     * Cập nhật số lượng của một món hàng trong giỏ
     */
    public void updateItemQuantity(String userId, String shoeId, String size, String color, int quantity) {
        if (quantity <= 0) {
            removeItem(userId, shoeId, size, color);
            return;
        }

        Cart cart = getCartByUserId(userId);
        if (cart.getItems() == null) {
            return;
        }

        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(i -> i.getShoeId().equals(shoeId)
                        && i.getSize().equalsIgnoreCase(size)
                        && (color == null || color.trim().isEmpty() || color.equalsIgnoreCase(i.getColor())))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            // Kiểm tra tồn kho thực tế từ Shoe
            Optional<Shoe> shoeOpt = shoeRepository.findById(shoeId);
            if (shoeOpt.isPresent()) {
                Shoe shoe = shoeOpt.get();
                int currentStock = 999;
                if (shoe.getVariants() != null) {
                    for (ShoeVariant v : shoe.getVariants()) {
                        if (v.getSize() != null && v.getSize().equalsIgnoreCase(size)) {
                            currentStock = v.getStock();
                            break;
                        }
                    }
                }
                item.setQuantity(Math.min(quantity, currentStock));
            } else {
                item.setQuantity(quantity);
            }
            cartRepository.save(cart);
        }
    }

    /**
     * Xóa một món hàng khỏi giỏ
     */
    public void removeItem(String userId, String shoeId, String size, String color) {
        Cart cart = getCartByUserId(userId);
        if (cart.getItems() != null) {
            cart.getItems().removeIf(i -> i.getShoeId().equals(shoeId)
                    && i.getSize().equalsIgnoreCase(size)
                    && (color == null || color.trim().isEmpty() || color.equalsIgnoreCase(i.getColor())));
            cartRepository.save(cart);
        }
    }

    /**
     * Xóa sạch các món trong giỏ hàng (sau khi đặt hàng thành công)
     */
    public void clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        if (cart.getItems() != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }
}
