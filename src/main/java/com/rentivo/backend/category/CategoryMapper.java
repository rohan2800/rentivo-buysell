package com.rentivo.backend.category;

import com.rentivo.backend.category.dto.CategoryDtos.CategoryResponse;
import com.rentivo.backend.category.dto.CategoryDtos.FieldResponse;

import java.util.Arrays;
import java.util.List;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    /** Public view lists only active fields; the admin view lists all. */
    public static CategoryResponse toResponse(Category c, boolean includeInactiveFields) {
        List<FieldResponse> fields = c.getFields().stream()
                .filter(f -> includeInactiveFields || f.isActive())
                .map(CategoryMapper::toResponse)
                .toList();
        return new CategoryResponse(c.getId(), c.getName(), c.isActive(), c.isSystemCategory(), fields);
    }

    public static FieldResponse toResponse(CategoryField f) {
        return new FieldResponse(f.getId(), f.getName(), f.getType(), f.isRequired(), options(f),
                f.getSortOrder(), f.isActive());
    }

    public static List<String> options(CategoryField f) {
        if (f.getOptionsCsv() == null || f.getOptionsCsv().isBlank()) {
            return List.of();
        }
        return Arrays.stream(f.getOptionsCsv().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
