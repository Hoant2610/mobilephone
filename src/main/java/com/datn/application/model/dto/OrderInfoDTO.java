package com.datn.application.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoDTO {
    private long id;

    private long totalPrice;

    private String sizeVn;

    private String sizeUs;

    private String sizeCm;

    private String productName;

    private String productImg;

//    private String color;

    public OrderInfoDTO(long id, long totalPrice, String sizeVn, String productName, String productImg) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.sizeVn = sizeVn;
        this.productName = productName;
        this.productImg = productImg;
//        this.color = color;
    }
}