package com.datn.application.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "product_size")
@IdClass(ProductSizeId.class)
public class ProductSize {
    @Id
    @Column(name = "size")
    private String size;
    @Id
    @Column(name = "product_id")
    private String productId;
    @Column(name = "quantity")
    private int quantity;
    @Column(name = "price")
    private long price;
    @Column(name = "sale_price")
    private long salePrice;
    @Id
    @Column(name = "color")
    private String color;
    @Column(name = "total_sold")
    private long totalSold;
}
