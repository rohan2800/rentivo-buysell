package com.rentivo.backend.category;

import com.rentivo.backend.category.dto.CategoryDtos.CategoryRequest;
import com.rentivo.backend.category.dto.CategoryDtos.CategoryResponse;
import com.rentivo.backend.category.dto.CategoryDtos.FieldRequest;
import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categories.findByActiveTrueOrderByNameAsc().stream()
                .map(c -> CategoryMapper.toResponse(c, false)).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getActive(Long id) {
        Category c = find(id);
        if (!c.isActive()) {
            throw new NotFoundException("Category not found");
        }
        return CategoryMapper.toResponse(c, false);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categories.findAllByOrderByNameAsc().stream()
                .map(c -> CategoryMapper.toResponse(c, true)).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        String name = req.name().trim();
        categories.findByNameIgnoreCase(name).ifPresent(x -> {
            throw new ConflictException("A category with this name already exists");
        });
        Category c = new Category();
        c.setName(name);
        c.setActive(req.active() == null || req.active());
        syncFields(c, req.fields());
        return CategoryMapper.toResponse(categories.saveAndFlush(c), true);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req) {
        Category c = find(id);
        String name = req.name().trim();
        categories.findByNameIgnoreCase(name).filter(x -> !x.getId().equals(id)).ifPresent(x -> {
            throw new ConflictException("A category with this name already exists");
        });
        c.setName(name);
        if (req.active() != null) {
            c.setActive(req.active());
        }
        syncFields(c, req.fields());
        return CategoryMapper.toResponse(categories.saveAndFlush(c), true);
    }

    @Transactional
    public CategoryResponse setActive(Long id, boolean active) {
        Category c = find(id);
        c.setActive(active);
        return CategoryMapper.toResponse(c, true);
    }

    private Category find(Long id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
    }

    /**
     * Reconciles the requested field list with what is stored. Existing fields keep their ids (so
     * saved listing values stay valid), missing ones are deactivated, new ones are inserted.
     * A field is never deleted.
     */
    private void syncFields(Category category, List<FieldRequest> requested) {
        if (requested == null) {
            return;
        }
        Map<Long, CategoryField> byId = new HashMap<>();
        Map<String, CategoryField> byName = new HashMap<>();
        for (CategoryField f : category.getFields()) {
            if (f.getId() != null) {
                byId.put(f.getId(), f);
            }
            byName.put(key(f.getName()), f);
        }

        Set<String> seenNames = new HashSet<>();
        Set<CategoryField> kept = new HashSet<>();
        for (FieldRequest r : requested) {
            String name = r.name().trim();
            if (!seenNames.add(key(name))) {
                throw new BadRequestException("Duplicate field name: " + name);
            }
            if ((r.type() == FieldType.SELECT || r.type() == FieldType.MULTI_SELECT)
                    && (r.optionsCsv() == null || r.optionsCsv().isBlank())) {
                throw new BadRequestException("Field '" + name + "' needs options");
            }

            CategoryField field;
            if (r.id() != null) {
                field = byId.get(r.id());
                if (field == null) {
                    throw new BadRequestException("Field " + r.id() + " does not belong to this category");
                }
            } else {
                // Re-adding a previously removed field reuses (and reactivates) the old row.
                field = byName.get(key(name));
                if (field == null) {
                    field = new CategoryField();
                    field.setCategory(category);
                    category.getFields().add(field);
                }
            }
            field.setName(name);
            field.setType(r.type());
            field.setRequired(r.required());
            field.setOptionsCsv(r.optionsCsv());
            field.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());
            field.setActive(true);
            kept.add(field);
        }
        for (CategoryField f : category.getFields()) {
            if (!kept.contains(f)) {
                f.setActive(false);
            }
        }
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
