package com.datn.application.controller.shop;

import com.datn.application.config.Contant;
import com.datn.application.entity.*;
import com.datn.application.model.dto.CheckPromotion;
import com.datn.application.model.dto.DetailProductInfoDTO;
import com.datn.application.model.dto.PageableDTO;
import com.datn.application.model.dto.ProductInfoDTO;
import com.datn.application.model.request.CreateOrderRequest;
import com.datn.application.model.request.FilterProductRequest;
import com.datn.application.security.CustomUserDetails;
import com.datn.application.service.*;
import com.datn.application.entity.*;
import com.datn.application.exception.BadRequestException;
import com.datn.application.exception.NotFoundException;
import com.datn.application.service.*;
import java.util.Collections;
import java.util.Comparator;
import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PromotionService promotionService;
    @GetMapping
    public String homePage(Model model){

        //Lấy 5 sản phẩm mới nhất
        List<ProductInfoDTO> newProducts = productService.getListNewProducts();
        model.addAttribute("newProducts", newProducts);

        //Lấy 5 sản phẩm bán chạy nhất
        List<ProductInfoDTO> bestSellerProducts = productService.getListBestSellProducts();
        model.addAttribute("bestSellerProducts", bestSellerProducts);

        //Lấy 5 sản phẩm có lượt xem nhiều
        List<ProductInfoDTO> viewProducts = productService.getListViewProducts();
        model.addAttribute("viewProducts", viewProducts);

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands",brands);

        return "shop/index";
    }

    @GetMapping("/{slug}/{id}")
    public String getProductDetail(Model model, @PathVariable String id){
        List<ProductSize> productSizes = new ArrayList<>();
        productSizes = productService.getListSizeOfProduct(id);
        List<String> sizes = new ArrayList<>();
        List<String> colors = new ArrayList<>();
        for(ProductSize ps : productSizes){
            if(!sizes.contains(ps.getSize())){
                sizes.add(ps.getSize());
            }
            if(!colors.contains(ps.getColor())){
                colors.add(ps.getColor());
            }
        }
        Collections.sort(sizes, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                // Trích xuất phần số từ chuỗi
                int num1 = Integer.parseInt(s1.replaceAll("\\D", ""));
                int num2 = Integer.parseInt(s2.replaceAll("\\D", ""));
                return Integer.compare(num1, num2);
            }
        });
        model.addAttribute("sizes",sizes);
        model.addAttribute("colors",colors);
        model.addAttribute("productSizes",productSizes);
        //Lấy thông tin sản phẩm
        DetailProductInfoDTO product;
        try {
            product = productService.getDetailProductById(id);
        } catch (NotFoundException ex) {
            return "error/404";
        } catch (Exception ex) {
            return "error/500";
        }
        model.addAttribute("product", product);

        //Lấy sản phẩm liên quan
        List<ProductInfoDTO> relatedProducts = productService.getRelatedProducts(id);
        model.addAttribute("relatedProducts", relatedProducts);

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands",brands);

        // Lấy size có sẵn
        List<String> availableSizes = productService.getListAvailableSize(id);
        model.addAttribute("availableSizes", availableSizes);
        if (!availableSizes.isEmpty()) {
            model.addAttribute("canBuy", true);
        } else {
            model.addAttribute("canBuy", false);
        }

        //Lấy danh sách
        model.addAttribute("sizeVn", Contant.SIZE_VN);
//        model.addAttribute("sizeUs", Contant.SIZE_US);
//        model.addAttribute("sizeCm", Contant.SIZE_CM);

        return "shop/detail";
    }

    @GetMapping("/dat-hang")
    public String getCartPage(Model model, @RequestParam String id,@RequestParam String size){

        //Lấy chi tiết sản phẩm
        DetailProductInfoDTO product;
        try {
            product = productService.getDetailProductById(id);
        } catch (NotFoundException ex) {
            return "error/404";
        } catch (Exception ex) {
            return "error/500";
        }
        model.addAttribute("product", product);

        //Validate size
        if (size.equalsIgnoreCase("S") && size.equalsIgnoreCase("L") && size.equalsIgnoreCase("XL") && size.equalsIgnoreCase("XXL") && size.equalsIgnoreCase("XXXL")) {
            return "error/404";
        }

        //Lấy danh sách size có sẵn
        List<String> availableSizes = productService.getListAvailableSize(id);
        model.addAttribute("availableSizes", availableSizes);
        boolean notFoundSize = true;
        for (String availableSize : availableSizes) {
            if (availableSize.equalsIgnoreCase(size)) {
                notFoundSize = false;
                break;
            }
        }
        model.addAttribute("notFoundSize", notFoundSize);

        //Lấy danh sách size
        model.addAttribute("sizeVn", Contant.SIZE_VN);
//        model.addAttribute("sizeUs", Contant.SIZE_US);
//        model.addAttribute("sizeCm", Contant.SIZE_CM);
        model.addAttribute("size", size);
        List<String> colors = productService.getAllColorsByProductIdAndSize(id,size);
        model.addAttribute("colors", colors);
//        model.addAttribute("num", colors.size());
        return "shop/payment";
    }

    @PostMapping("/api/orders")
    public ResponseEntity<Object> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        //  lấy thông tin người dùng đã được xác thực là thông qua SecurityContextHolder
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        Order order = orderService.createOrder(createOrderRequest, user.getId());

        //Nếu tạo thành công return 200 OK
        return ResponseEntity.ok(order.getId());
    }

    @GetMapping("/products")
    public ResponseEntity<Object> getListBestSellProducts(){
        List<ProductInfoDTO> productInfoDTOS = productService.getListBestSellProducts();
        return ResponseEntity.ok(productInfoDTOS);
    }

    @GetMapping("/san-pham")
    public String getProductShopPages(Model model){

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands",brands);
        List<Long> brandIds = new ArrayList<>();
        for (Brand brand : brands) {
            brandIds.add(brand.getId());
        }
        model.addAttribute("brandIds", brandIds);

        //Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories",categories);
        List<Long> categoryIds = new ArrayList<>();
        for (Category category : categories) {
            categoryIds.add(category.getId());
        }
        model.addAttribute("categoryIds", categoryIds);

        //Danh sách size của sản phẩm
        model.addAttribute("sizeVn", Contant.SIZE_VN);

        //Lấy danh sách sản phẩm
        FilterProductRequest req = new FilterProductRequest(brandIds, categoryIds, new ArrayList<>(), (long) 0, Long.MAX_VALUE, 1);
        PageableDTO result = productService.filterProduct(req);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("currentPage", result.getCurrentPage());
        model.addAttribute("listProduct", result.getItems());

        return "shop/product";
    }

    @PostMapping("/api/san-pham/loc")
    public ResponseEntity<?> filterProduct(@RequestBody FilterProductRequest req) {
        // Validate
        if (req.getMinPrice() == null) {
            req.setMinPrice((long) 0);
        } else {
            if (req.getMinPrice() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mức giá phải lớn hơn 0");
            }
        }
        if (req.getMaxPrice() == null) {
            req.setMaxPrice(Long.MAX_VALUE);
        } else {
            if (req.getMaxPrice() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mức giá phải lớn hơn 0");
            }
        }

        PageableDTO result = productService.filterProduct(req);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/tim-kiem")
    public String searchProduct(Model model, @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer page) {

        PageableDTO result = productService.searchProductByKeyword(keyword, page);

        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("currentPage", result.getCurrentPage());
        model.addAttribute("listProduct", result.getItems());
        model.addAttribute("keyword", keyword);
        if (((List<?>) result.getItems()).isEmpty()) {
            model.addAttribute("hasResult", false);
        } else {
            model.addAttribute("hasResult", true);
        }

        return "shop/search";
    }

    @GetMapping("/api/check-hidden-promotion")
    public ResponseEntity<Object> checkPromotion(@RequestParam String code) {
        if (code == null || code == "") {
            throw new BadRequestException("Mã code trống");
        }

        Promotion promotion = promotionService.checkPromotion(code);
        if (promotion == null) {
            throw new BadRequestException("Mã code không hợp lệ");
        }
        CheckPromotion checkPromotion = new CheckPromotion();
        checkPromotion.setDiscountType(promotion.getDiscountType());
        checkPromotion.setDiscountValue(promotion.getDiscountValue());
        checkPromotion.setMaximumDiscountValue(promotion.getMaximumDiscountValue());
        return ResponseEntity.ok(checkPromotion);
    }

    @GetMapping("lien-he")
    public String contact(){
        return "shop/lien-he";
    }
    @GetMapping("huong-dan")
    public String buyGuide(){
        return "shop/buy-guide";
    }
    @GetMapping("doi-hang")
    public String doiHang(){
        return "shop/doi-hang";
    }

}
