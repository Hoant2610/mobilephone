package com.datn.application.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Contant {

    // 7 ngày
    public static final int MAX_AGE_COOKIE = 7 * 24 * 60 * 60;

    //Limit
    public static final int LIMIT_BRAND = 10;
    public static final int LIMIT_CATEGORY = 10;
    public static final int LIMIT_PRODUCT = 5;
    public static final int LIMIT_POST = 10;
    public static final int LIMIT_POST_SHOP = 8;
    public static final int LIMIT_POST_RELATED = 5;
    public static final int LIMIT_USER = 5;

    //Product
    public static final int LIMIT_PRODUCT_SELL = 10;
    public static final int LIMIT_PRODUCT_NEW = 8;
    public static final int LIMIT_PRODUCT_VIEW = 8;
    public static final int LIMIT_PRODUCT_RELATED = 8;
    public static final int LIMIT_PRODUCT_SHOP = 5;
    public static final int LIMIT_PRODUCT_SEARCH = 5;

    //Post
    public static final int LIMIT_POST_NEW = 5;

    //Size
    public static final List<String> SIZE_VN = new ArrayList<>(Arrays.asList("64G", "128G", "256G", "1T"));
//    public static final String[] SIZE_US = {"S", "L", "XL", "XXL", "XXXL"};
//    public static final String[] SIZE_CM = {"S", "L", "XL", "XXL", "XXXL"};

    //Trạng thái post
    public static final int PUBLIC_POST = 1;
    public static final int DRAFT_POST = 0;

    //Trạng thái đơn hàng
    public static final int ORDER_STATUS = 1;
    public static final int DELIVERY_STATUS = 2;
    public static final int COMPLETED_STATUS = 3;
    public static final int RETURNED_STATUS = 4;
    public static final int CANCELED_STATUS = 5;

    public static final int PAYMENT_STATUS = 6;
    public static final List<Integer> LIST_ORDER_STATUS = new ArrayList<>(Arrays.asList(ORDER_STATUS,DELIVERY_STATUS, COMPLETED_STATUS, RETURNED_STATUS, CANCELED_STATUS,PAYMENT_STATUS));

    //Loại khuyến mại
    public static final int DISCOUNT_PERCENT = 1;
    public static final int DISCOUNT_AMOUNT = 2;
}
