package com.datn.application.service;

import com.datn.application.entity.Product;
import com.datn.application.entity.ProductCart;
import com.datn.application.entity.User;
import com.datn.application.repository.ProductCartRepository;
import com.datn.application.repository.ProductRepository;
import com.datn.application.security.CustomUserDetails;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProductCartService {
    @Autowired
    private ProductCartRepository productCartRepository;
  @Autowired
  private ProductRepository productRepository;

  public ProductCartService(ProductCartRepository productCartRepository,ProductRepository productRepository) {
    this.productCartRepository = productCartRepository;
    this.productRepository = productRepository;
  }

  public List<ProductCart> findProductCartsByUser(User user){
      return productCartRepository.findProductCartsByUser(user);
    }

  public ResponseEntity<?> addProduct(String productId, long quantity) {
    Product product = productRepository.findById(productId).get();
    User user =((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    List<ProductCart> productCarts = productCartRepository.findProductCartsByUser(user);
//    int check = 0;
    for(ProductCart p : productCarts){
      if(p.getProduct().getId().equals(product.getId())){
//        check = 1;
        p.setQuantity(p.getQuantity() + quantity);
        productCartRepository.save(p);
        return ResponseEntity.ok(p);
      }
    }
    ProductCart p = new ProductCart();
    p.setUser(user);
    p.setProduct(product);
    p.setQuantity(quantity);
    productCartRepository.save(p);
    return ResponseEntity.ok(p);
  }
  public ResponseEntity<?> decreaseProduct(String productId) {
    Product product = productRepository.findById(productId).get();
    User user =((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    List<ProductCart> productCarts = productCartRepository.findProductCartsByUser(user);
    for(ProductCart p : productCarts){
      if(p.getProduct().getId().equals(product.getId())){
        if(p.getQuantity() != 1){
          p.setQuantity(p.getQuantity() - 1);
          productCartRepository.save(p);
          return ResponseEntity.ok(p);
        }
        if(p.getQuantity() == 1){
          productCartRepository.delete(p);
        }
      }
    }
    return ResponseEntity.ok("Cập nhật giỏ hàng thành công!");
  }
}
