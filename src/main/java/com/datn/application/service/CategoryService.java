package com.datn.application.service;

import com.datn.application.entity.Category;
import com.datn.application.model.request.CreateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<Category> getListCategories();

    Category getCategoryById(long id);

    Category createCategory(CreateCategoryRequest createCategoryRequest);

    void updateCategory(CreateCategoryRequest createCategoryRequest, Long id);

    void deleteCategory(long id);

    Page<Category> adminGetListCategory(String id, String name, String status, int page);

    void updateOrderCategory(int[] ids);

    //Đếm số danh mục
    long getCountCategories();
}
