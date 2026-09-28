package com.rentivo.backend.category.dto;

import com.rentivo.backend.category.FieldType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    /** id == null creates a field; an existing id updates it. Fields missing from the list are deactivated. */
    public record FieldRequest(Long id,
                               @NotBlank @Size(max = 100) String name,
                               @NotNull FieldType type,
                               boolean required,
                               @Size(max = 2000) String optionsCsv,
                               Integer sortOrder) {
    }

    public record CategoryRequest(@NotBlank @Size(max = 100) String name,
                                  Boolean active,
                                  @Valid List<FieldRequest> fields) {
    }

    public record FieldResponse(Long id, String name, FieldType type, boolean required,
                                List<String> options, int sortOrder, boolean active) {
    }

    public record CategoryResponse(Long id, String name, boolean active, boolean systemCategory,
                                   List<FieldResponse> fields) {
    }
}
