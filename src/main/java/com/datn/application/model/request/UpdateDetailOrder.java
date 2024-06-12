package com.datn.application.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UpdateDetailOrder {
    @NotBlank(message = "Sản phẩm trống")
    @JsonProperty("product_id")
    private String productId;

    private String size;

    private String color;

    @JsonProperty("coupon_code")
    private String couponCode;

    @JsonProperty("total_price")
    private long totalPrice;

    @JsonProperty("product_price")
    private long productPrice;
}
