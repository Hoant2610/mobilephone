package com.datn.application.controller.admin;

import com.datn.application.config.Contant;
import com.datn.application.entity.*;
import com.datn.application.model.request.CreateProductRequest;
import com.datn.application.model.request.CreateSizeCountRequest;
import com.datn.application.model.request.UpdateFeedBackRequest;
import com.datn.application.security.CustomUserDetails;
import com.datn.application.service.BrandService;
import com.datn.application.service.CategoryService;
import com.datn.application.service.ImageService;
import com.datn.application.service.ProductService;
import com.datn.application.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Controller
public class ProductController {

    private String xlsx = ".xlsx";
    private static final int BUFFER_SIZE = 4096;
    private static final String TEMP_EXPORT_DATA_DIRECTORY = "\\resources\\reports";
    private static final String EXPORT_DATA_REPORT_FILE_NAME = "San_pham";

    @Autowired
    private ServletContext context;

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/admin/products")
    public String homePages(Model model,
                            @RequestParam(defaultValue = "", required = false) String id,
                            @RequestParam(defaultValue = "", required = false) String name,
                            @RequestParam(defaultValue = "", required = false) String category,
                            @RequestParam(defaultValue = "", required = false) String brand,
                            @RequestParam(defaultValue = "1", required = false) Integer page) {

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands", brands);
        //Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories", categories);
        //Lấy danh sách sản phẩm
        Page<Product> products = productService.adminGetListProduct(id, name, category, brand, page);
        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", products.getPageable().getPageNumber() + 1);

        return "admin/product/list";
    }

    @GetMapping("/admin/products/create")
    public String getProductCreatePage(Model model) {
        //Lấy danh sách anh của user
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        //Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands", brands);
        //Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories", categories);

        return "admin/product/create";
    }

    @GetMapping("/admin/products/{slug}/{id}")
    public String getProductUpdatePage(Model model, @PathVariable String id) {

        // Lấy thông tin sản phẩm theo id
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);

        // Lấy danh sách ảnh của user
        User user = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
        List<String> images = imageService.getListImageOfUser(user.getId());
        model.addAttribute("images", images);

        // Lấy danh sách danh mục
        List<Category> categories = categoryService.getListCategories();
        model.addAttribute("categories", categories);

        // Lấy danh sách nhãn hiệu
        List<Brand> brands = brandService.getListBrand();
        model.addAttribute("brands", brands);

        //Lấy danh sách size
        model.addAttribute("sizeVN", Contant.SIZE_VN);

        //Lấy size của sản phẩm
        List<ProductSize> productSizes = productService.getListSizeOfProduct(id);
        model.addAttribute("productSizes", productSizes);
        //Lấ tất cả màu
        List<String> colors = productService.getAllColors();
        model.addAttribute("colors", colors);
        return "admin/product/edit";
    }

    @GetMapping("/api/admin/products")
    public ResponseEntity<Object> getListProducts(@RequestParam(defaultValue = "", required = false) String id,
                                                  @RequestParam(defaultValue = "", required = false) String name,
                                                  @RequestParam(defaultValue = "", required = false) String category,
                                                  @RequestParam(defaultValue = "", required = false) String brand,
                                                  @RequestParam(defaultValue = "1", required = false) Integer page) {
        Page<Product> products = productService.adminGetListProduct(id, name, category, brand, page);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/api/admin/products/{id}")
    public ResponseEntity<Object> getProductDetail(@PathVariable String id) {
        Product rs = productService.getProductById(id);
        return ResponseEntity.ok(rs);
    }

    @PostMapping("/api/admin/products")
    public ResponseEntity<Object> createProduct(@Valid @RequestBody CreateProductRequest createProductRequest) {
        Product product = productService.createProduct(createProductRequest);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/api/admin/products/{id}")
    public ResponseEntity<Object> updateProduct(@Valid @RequestBody CreateProductRequest createProductRequest, @PathVariable String id) {
        productService.updateProduct(createProductRequest, id);
        return ResponseEntity.ok("Sửa sản phẩm thành công!");
    }

    @DeleteMapping("/api/admin/products")
    public ResponseEntity<Object> deleteProduct(@RequestBody String[] ids) {
        productService.deleteProduct(ids);
        return ResponseEntity.ok("Xóa sản phẩm thành công!");
    }

    @DeleteMapping("/api/admin/products/{id}")
    public ResponseEntity<Object> deleteProductById(@PathVariable String id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công!");
    }

    @PutMapping("/api/admin/products/sizes")
    public ResponseEntity<?> updateSizeCount(@Valid @RequestBody CreateSizeCountRequest createSizeCountRequest) {
        productService.createSizeCount(createSizeCountRequest);

        return ResponseEntity.ok("Cập nhật thành công!");
    }

    @PutMapping("/api/admin/products/{id}/update-feedback-image")
    public ResponseEntity<?> updatefeedBackImages(@PathVariable String id, @Valid @RequestBody UpdateFeedBackRequest req) {
        productService.updatefeedBackImages(id, req);

        return ResponseEntity.ok("Cập nhật thành công");
    }

    @GetMapping("/api/products/export/excel")
    public void exportProductDataToExcelFile(HttpServletResponse response) {
        List<Product> result = productService.getAllProduct();
        String fullPath = this.generateProductExcel(result, context, EXPORT_DATA_REPORT_FILE_NAME);
        if (fullPath != null) {
            this.fileDownload(fullPath, response, EXPORT_DATA_REPORT_FILE_NAME, "xlsx");
        }
    }

    private String generateProductExcel(List<Product> products, ServletContext context, String fileName) {
        String filePath = context.getRealPath(TEMP_EXPORT_DATA_DIRECTORY);
        File file = new File(filePath);
        if (!file.exists()) {
            new File(filePath).mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file + "\\" + fileName + xlsx);
             XSSFWorkbook workbook = new XSSFWorkbook();) {

            XSSFSheet worksheet = workbook.createSheet("Product");
            worksheet.setDefaultColumnWidth(20);
            worksheet.addMergedRegion(CellRangeAddress.valueOf("A1:F1"));
            worksheet.addMergedRegion(CellRangeAddress.valueOf("A2:F2"));
            worksheet.addMergedRegion(CellRangeAddress.valueOf("A3:F5"));


            XSSFRow titleRow = worksheet.createRow(0);
            XSSFCellStyle titleCellStyle = workbook.createCellStyle();
            XSSFFont titlefont = workbook.createFont();
            titlefont.setColor(new XSSFColor(java.awt.Color.WHITE));
            titlefont.setFontName("Comic Sans MS");
            titlefont.setBold(true);
            titlefont.setFontHeightInPoints((short) 30);
            titleCellStyle.setAlignment(HorizontalAlignment.CENTER);;
            titleCellStyle.setFont(titlefont);
            titleCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(180, 122, 3)));
            titleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFCell title1 = titleRow.createCell(0);
            title1.setCellValue("Danh sách sản phẩm");
            title1.setCellStyle(titleCellStyle);

            XSSFRow titleRow2 = worksheet.createRow(1);
            XSSFCellStyle titleCellStyle2 = workbook.createCellStyle();
            XSSFFont titlefont2 = workbook.createFont();
            titlefont2.setFontName("Tahoma");
            titlefont2.setBold(true);
            titlefont2.setFontHeightInPoints((short) 15);
            titleCellStyle.setAlignment(HorizontalAlignment.CENTER);;
            titlefont2.setColor(new XSSFColor(Color.BLACK));
            titleCellStyle2.setAlignment(HorizontalAlignment.CENTER);;
            titleCellStyle2.setFont(titlefont2);
            titleCellStyle2.setFillForegroundColor(new XSSFColor(new java.awt.Color(180, 122, 3)));
            titleCellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFCell title2 = titleRow2.createCell(0);
            title2.setCellValue("Báo cáo: 5/2023");
            title2.setCellStyle(titleCellStyle2);

            XSSFRow titleRow3 = worksheet.createRow(2);
            XSSFCellStyle titleCellStyle3 = workbook.createCellStyle();
            XSSFFont titlefont3 = workbook.createFont();
            titlefont3.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            titlefont3.setColor(new XSSFColor(Color.BLACK));
            titleCellStyle3.setFont(titlefont3);
            titleCellStyle3.setVerticalAlignment(VerticalAlignment.TOP);;
            titleCellStyle3.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 255)));
            titleCellStyle3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFCell title3 = titleRow3.createCell(0);
            title3.setCellStyle(titleCellStyle3);
            title3.setCellValue("Ghi chú:");


            XSSFRow headerRow = worksheet.createRow(5);

            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
            font.setColor(new XSSFColor(java.awt.Color.WHITE));
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);;
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);

            headerCellStyle.setFont(font);
            headerCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(135, 206, 250)));
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFCell productId = headerRow.createCell(0);
            productId.setCellValue("Mã sản phẩm");
            productId.setCellStyle(headerCellStyle);

            XSSFCell productName = headerRow.createCell(1);
            productName.setCellValue("Tên sản phẩm");
            productName.setCellStyle(headerCellStyle);

            XSSFCell productBrand = headerRow.createCell(2);
            productBrand.setCellValue("Thương hiệu");
            productBrand.setCellStyle(headerCellStyle);

            XSSFCell price = headerRow.createCell(3);
            price.setCellValue("Giá nhập");
            price.setCellStyle(headerCellStyle);

            XSSFCell priceSell = headerRow.createCell(4);
            priceSell.setCellValue("Giá bán");
            priceSell.setCellStyle(headerCellStyle);

            XSSFCell totalSold = headerRow.createCell(5);
            totalSold.setCellValue("Đã bán");
            totalSold.setCellStyle(headerCellStyle);

            int i = 0;
            if (!products.isEmpty()) {
                for (i = 0; i < products.size(); i++) {
                    Product product = products.get(i);
                    XSSFRow bodyRow = worksheet.createRow(i + 6);
                    XSSFCellStyle bodyCellStyle = workbook.createCellStyle();
                    bodyCellStyle.setBorderTop(BorderStyle.THIN);
                    bodyCellStyle.setBorderBottom(BorderStyle.THIN);
                    bodyCellStyle.setBorderLeft(BorderStyle.THIN);
                    bodyCellStyle.setBorderRight(BorderStyle.THIN);
                    bodyCellStyle.setAlignment(HorizontalAlignment.CENTER);;
                    bodyCellStyle.setFillForegroundColor(new XSSFColor(java.awt.Color.WHITE));

                    XSSFCell productIDValue = bodyRow.createCell(0);
                    productIDValue.setCellValue(product.getId());
                    productIDValue.setCellStyle(bodyCellStyle);

                    XSSFCell productNameValue = bodyRow.createCell(1);
                    productNameValue.setCellValue(product.getName());
                    productNameValue.setCellStyle(bodyCellStyle);

                    XSSFCell productBrandValue = bodyRow.createCell(2);
                    productBrandValue.setCellValue(product.getBrand().getName());
                    productBrandValue.setCellStyle(bodyCellStyle);

                    XSSFCell priceValue = bodyRow.createCell(3);
                    priceValue.setCellValue(product.getPrice());
                    priceValue.setCellStyle(bodyCellStyle);

                    XSSFCell priceSellValue = bodyRow.createCell(4);
                    priceSellValue.setCellValue(product.getSalePrice());
                    priceSellValue.setCellStyle(bodyCellStyle);

                    XSSFCell totalSoldValue = bodyRow.createCell(5);
                    totalSoldValue.setCellValue(product.getTotalSold());
                    totalSoldValue.setCellStyle(bodyCellStyle);
                }
            }
            int j = i + 7;
            worksheet.addMergedRegion(CellRangeAddress.valueOf("A" + j + ":" + "F" + j));
            worksheet.addMergedRegion(CellRangeAddress.valueOf("E" + (j + 1) + ":" + "F" + (j + 1)));
            worksheet.addMergedRegion(CellRangeAddress.valueOf("A" + (j + 1) + ":" + "D" + (j + 1)));
            worksheet.addMergedRegion(CellRangeAddress.valueOf("A" + (j + 2) + ":" + "D" + (j + 6)));
            worksheet.addMergedRegion(CellRangeAddress.valueOf("E" + (j + 2) + ":" + "F" + (j + 6)));

            XSSFRow footerROW = worksheet.createRow(j);
            XSSFCell signCell = footerROW.createCell(4);
            XSSFRow footerROW2 = worksheet.createRow(j+1);
            XSSFCell signCell2 = footerROW2.createCell(4);

            XSSFCellStyle footerStyle = workbook.createCellStyle();
            XSSFFont footerfont2 = workbook.createFont();
            footerfont2.setFontName("Tahoma");
            footerfont2.setBold(true);
            footerfont2.setFontHeightInPoints((short) 12);
            footerfont2.setColor(new XSSFColor(Color.BLACK));
            footerStyle.setAlignment(HorizontalAlignment.CENTER);;
            footerStyle.setVerticalAlignment(VerticalAlignment.TOP);;
            footerStyle.setFont(footerfont2);
            footerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255,255,255)));
            footerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            signCell.setCellValue("Người tạo báo cáo");
            signCell.setCellStyle(footerStyle);

            signCell2.setCellStyle(footerStyle);
            signCell2.setCellValue("Ký tên:");

            workbook.write(fos);
            return file + "\\" + fileName + xlsx;
        } catch (Exception e) {
            return null;
        }
    }
    private void fileDownload(String fullPath, HttpServletResponse response, String fileName, String type) {
        File file = new File(fullPath);
        if (file.exists()) {
            OutputStream os = null;
            try(FileInputStream fis = new FileInputStream(file);) {
                String mimeType = context.getMimeType(fullPath);
                response.setContentType(mimeType);
                response.setHeader("content-disposition", "attachment; filename=" + fileName + "." + type);
                os = response.getOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = -1;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                Files.delete(file.toPath());
            } catch (Exception e) {
                log.error("Can't download file, detail: {}", e.getMessage());
            } finally {
                if(os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
