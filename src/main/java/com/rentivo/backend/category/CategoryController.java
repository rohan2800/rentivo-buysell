package com.rentivo.backend.category;

import com.rentivo.backend.category.dto.CategoryDtos.CategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return service.listActive();
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id) {
        return service.getActive(id);
    }
}
