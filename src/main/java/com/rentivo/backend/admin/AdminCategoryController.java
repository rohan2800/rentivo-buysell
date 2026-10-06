package com.rentivo.backend.admin;

import com.rentivo.backend.audit.AdminAuditService;
import com.rentivo.backend.category.CategoryService;
import com.rentivo.backend.category.dto.CategoryDtos.CategoryRequest;
import com.rentivo.backend.category.dto.CategoryDtos.CategoryResponse;
import com.rentivo.backend.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService service;
    private final AdminAuditService audit;

    public AdminCategoryController(CategoryService service, AdminAuditService audit) {
        this.service = service;
        this.audit = audit;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return service.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request, @AuthenticationPrincipal AuthUser admin) {
        CategoryResponse result = service.create(request);
        audit.record(admin, "CATEGORY_CREATED", "CATEGORY", result.id(), result.name());
        return result;
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request,
                                   @AuthenticationPrincipal AuthUser admin) {
        CategoryResponse result = service.update(id, request);
        audit.record(admin, "CATEGORY_UPDATED", "CATEGORY", id, result.name());
        return result;
    }

    @PatchMapping("/{id}/active")
    public CategoryResponse setActive(@PathVariable Long id, @RequestParam boolean value,
                                      @AuthenticationPrincipal AuthUser admin) {
        CategoryResponse result = service.setActive(id, value);
        audit.record(admin, value ? "CATEGORY_ACTIVATED" : "CATEGORY_DEACTIVATED", "CATEGORY", id, result.name());
        return result;
    }
}
