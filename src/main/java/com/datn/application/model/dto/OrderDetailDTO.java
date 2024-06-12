package com.datn.application.model.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private long id;

    private long totalPrice;

    private long productPrice;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private int status;

    private String statusText;

    private String sizeVn;

    private String sizeUs;

    private String sizeCm;

    private String productName;

    private String productImg;

    private Integer quantity;

    private String color;
    public OrderDetailDTO (long id, long totalPrice, long productPrice, String receiverName, String receiverPhone, String receiverAddress, int status, String sizeVn, String productName, String productImg,String color, int quantity) {
        this.color = color;
        this.quantity = quantity;
        this.id = id;
        this.totalPrice = totalPrice;
        this.productPrice = productPrice;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress = receiverAddress;
        this.status = status;
        this.sizeVn = sizeVn;
        this.productName = productName;
        this.productImg = productImg;

    }
}
