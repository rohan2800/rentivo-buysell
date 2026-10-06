package com.rentivo.backend.category;

import com.rentivo.backend.category.dto.CategoryDtos.CategoryRequest;
import com.rentivo.backend.category.dto.CategoryDtos.CategoryResponse;
import com.rentivo.backend.category.dto.CategoryDtos.FieldRequest;
import com.rentivo.backend.common.exception.BadRequestException;
import com.rentivo.backend.common.exception.ConflictException;
import com.rentivo.backend.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryServiceTest {

    private static final long ID = 1L;

    @Mock CategoryRepository categories;

    private final CategoryService service = new CategoryService(categories);

    @Test
    void createRejectsADuplicateNameCaseInsensitively() {
        Category existing = new Category();
        existing.setName("Furniture");
        when(categories.findByNameIgnoreCase("furniture")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class,
                () -> service.create(new CategoryRequest("furniture", true, List.of())));
    }

    @Test
    void selectFieldWithoutOptionsIsRejected() {
        when(categories.findByNameIgnoreCase("Vehicles")).thenReturn(Optional.empty());
        when(categories.saveAndFlush(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        FieldRequest badSelect = new FieldRequest(null, "Color", FieldType.SELECT, false, null, 0);
        assertThrows(BadRequestException.class,
                () -> service.create(new CategoryRequest("Vehicles", true, List.of(badSelect))));
    }

    @Test
    void duplicateFieldNamesAreRejected() {
        when(categories.findByNameIgnoreCase("Vehicles")).thenReturn(Optional.empty());
        when(categories.saveAndFlush(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        FieldRequest a = new FieldRequest(null, "Brand", FieldType.TEXT, false, null, 0);
        FieldRequest b = new FieldRequest(null, "brand", FieldType.TEXT, false, null, 1);
        assertThrows(BadRequestException.class,
                () -> service.create(new CategoryRequest("Vehicles", true, List.of(a, b))));
    }

    @Test
    void updateReactivatesAPreviouslyRemovedFieldByName() {
        Category existing = new Category();
        ReflectionTestUtils.setField(existing, "id", ID);
        existing.setName("Furniture");
        CategoryField old = new CategoryField();
        ReflectionTestUtils.setField(old, "id", 5L);
        old.setCategory(existing);
        old.setName("Material");
        old.setType(FieldType.TEXT);
        old.setActive(false);
        existing.getFields().add(old);
        when(categories.findById(ID)).thenReturn(Optional.of(existing));
        when(categories.findByNameIgnoreCase("Furniture")).thenReturn(Optional.of(existing));

        FieldRequest bringBack = new FieldRequest(null, "Material", FieldType.TEXT, false, null, 0);
        CategoryResponse res = service.update(ID, new CategoryRequest("Furniture", true, List.of(bringBack)));

        assertEquals(1, res.fields().size());
        assertEquals(5L, res.fields().get(0).id());
    }

    @Test
    void fieldsOmittedFromTheUpdateAreDeactivatedNotDeleted() {
        Category existing = new Category();
        ReflectionTestUtils.setField(existing, "id", ID);
        existing.setName("Furniture");
        CategoryField kept = new CategoryField();
        ReflectionTestUtils.setField(kept, "id", 1L);
        kept.setCategory(existing);
        kept.setName("Material");
        kept.setType(FieldType.TEXT);
        kept.setActive(true);
        existing.getFields().add(kept);
        when(categories.findById(ID)).thenReturn(Optional.of(existing));
        when(categories.findByNameIgnoreCase("Furniture")).thenReturn(Optional.of(existing));

        service.update(ID, new CategoryRequest("Furniture", true, List.of()));

        assertFalse(kept.isActive());
    }

    @Test
    void settingActiveOnAMissingCategoryFails() {
        when(categories.findById(ID)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.setActive(ID, false));
    }
}
