package com.datn.application.repository;

import com.datn.application.entity.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize,Long> {

    //Lấy size của sản phẩm
    @Query(nativeQuery = true,value = "SELECT ps.size FROM product_size ps WHERE ps.product_id = ?1 AND ps.quantity > 0")
    List<String> findAllSizeOfProduct(String id);

    List<ProductSize> findByProductId(String id);

    //Kiểm trả size sản phẩm
    @Query(value = "SELECT DISTINCT * FROM product_size WHERE product_id = ?1 AND size = ?2 AND color = ?3 AND quantity >0", nativeQuery = true)
    ProductSize checkProductAndSizeAvailable(String id, String size,String color);

    //Trừ 1 sản phẩm theo size
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "Update product_size set quantity = quantity - 1 where product_id = ?1 and size = ?2")
    public void minusOneProductBySize(String id, String size);

    //Cộng 1 sản phẩm theo size
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "Update product_size set quantity = quantity + ?3 where product_id = ?1 and size = ?2")
    public void plusProductBySize(String id, String size,int quantity);

//    @Query(value = "SELECT * FROM product_size ps WHERE ps.size = ?1 AND ps.product_id = ?2",nativeQuery = true)
//    Optional<ProductSize> getProductSizeBySize(int size,String productId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "Delete from product_size where product_id = ?1")
    public void deleteByProductId(String id);

    @Query(nativeQuery = true,value = "SELECT DISTINCT ps.color FROM product_size ps")
    List<String> findAllColors();

    @Query(nativeQuery = true,value = "SELECT DISTINCT ps.color FROM product_size ps where ps.product_id = :productId and ps.size = :size and ps.quantity > 0")
    List<String> findColorsByProductIdAndSize(String productId, String size);
//    Lấy ra giá nhỏ nhất của 1 sản phẩm bằng id
    @Query(value = "SELECT MIN(sale_price) FROM product_size WHERE product_id = :productId", nativeQuery = true)
    Long findMinSalePriceByProductId(String productId);
    @Query(value = "SELECT MAX(sale_price) FROM product_size WHERE product_id = :productId", nativeQuery = true)
    Long findMaxSalePriceByProductId(String productId);
    @Query(nativeQuery = true,value = "SELECT * FROM product_size ps where ps.product_id = :productId and ps.size = :size and ps.color = :color ")
    ProductSize findByProductIdAndSizeAndColor(String productId, String size, String color);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "Update product_size set total_sold = total_sold + ?4 where id = ?1 "
        + "AND size = ?2 AND color = ?3")
    void plusProductTotalSold(String productId, String size, String color,int quantity);
}
