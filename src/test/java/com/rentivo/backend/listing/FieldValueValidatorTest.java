package com.rentivo.backend.listing;

import com.rentivo.backend.category.Category;
import com.rentivo.backend.category.CategoryField;
import com.rentivo.backend.category.FieldType;
import com.rentivo.backend.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldValueValidatorTest {

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Property");
        add(1L, "Bedrooms", FieldType.NUMBER, true, null, true);
        add(2L, "Furnishing", FieldType.SELECT, false, "Furnished,Semi-furnished,Unfurnished", true);
        add(3L, "Amenities", FieldType.MULTI_SELECT, false, "Lift,Parking,Gym", true);
        add(4L, "Old field", FieldType.TEXT, false, null, false);
    }

    private void add(Long id, String name, FieldType type, boolean required, String options, boolean active) {
        CategoryField f = new CategoryField();
        f.setId(id);
        f.setCategory(category);
        f.setName(name);
        f.setType(type);
        f.setRequired(required);
        f.setOptionsCsv(options);
        f.setActive(active);
        category.getFields().add(f);
    }

    @Test
    void acceptsValidValuesAndNormalisesThem() {
        Map<Long, String> input = new HashMap<>();
        input.put(1L, " 3 ");
        input.put(2L, "Furnished");
        input.put(3L, "Lift, Gym, Lift");

        Map<CategoryField, String> result = FieldValueValidator.validate(category, input);

        assertEquals(3, result.size());
        assertTrue(result.containsValue("3"));
        assertTrue(result.containsValue("Lift,Gym"));
    }

    @Test
    void requiredFieldMustBeFilled() {
        assertThrows(BadRequestException.class, () -> FieldValueValidator.validate(category, Map.of()));
        assertThrows(BadRequestException.class, () -> FieldValueValidator.validate(category, Map.of(1L, "  ")));
    }

    @Test
    void optionalBlankFieldsAreSkipped() {
        Map<CategoryField, String> result = FieldValueValidator.validate(category, Map.of(1L, "2"));
        assertEquals(1, result.size());
    }

    @Test
    void rejectsWrongTypes() {
        assertThrows(BadRequestException.class, () -> FieldValueValidator.validate(category, Map.of(1L, "three")));
        assertThrows(BadRequestException.class,
                () -> FieldValueValidator.validate(category, Map.of(1L, "2", 2L, "Castle")));
        assertThrows(BadRequestException.class,
                () -> FieldValueValidator.validate(category, Map.of(1L, "2", 3L, "Lift,Helipad")));
    }

    @Test
    void rejectsUnknownAndInactiveFields() {
        assertThrows(BadRequestException.class, () -> FieldValueValidator.validate(category, Map.of(1L, "2", 99L, "x")));
        assertThrows(BadRequestException.class, () -> FieldValueValidator.validate(category, Map.of(1L, "2", 4L, "x")));
    }
}
