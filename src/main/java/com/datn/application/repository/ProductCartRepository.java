package com.datn.application.repository;

import com.datn.application.entity.Product;
import com.datn.application.entity.ProductCart;
import com.datn.application.entity.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCartRepository extends JpaRepository<ProductCart, Long> {
    List<ProductCart> findProductCartsByUser(User user);
}
