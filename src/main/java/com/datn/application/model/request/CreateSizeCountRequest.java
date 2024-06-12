package com.datn.application.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateSizeCountRequest {
    private String size;

    @Min(0)
    private int count;
    private long price;
    private long salePrice;
    private String color;
    @NotEmpty(message = "Mã sản phẩm trống")
    @NotNull(message = "Mã sản phẩm trống")
    @JsonProperty("product_id")
    private String productId;
}
