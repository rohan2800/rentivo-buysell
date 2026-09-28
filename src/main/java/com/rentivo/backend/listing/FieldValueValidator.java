package com.rentivo.backend.listing;

import com.rentivo.backend.category.Category;
import com.rentivo.backend.category.CategoryField;
import com.rentivo.backend.category.CategoryMapper;
import com.rentivo.backend.common.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Validates admin-defined dynamic fields against their declared types. */
public final class FieldValueValidator {

    private static final int MAX_TEXT = 255;
    private static final int MAX_TEXTAREA = 4000;

    private FieldValueValidator() {
    }

    /** Returns normalised values for every field that was filled in, keyed by field. */
    public static Map<CategoryField, String> validate(Category category, Map<Long, String> input) {
        Map<Long, String> values = input == null ? Map.of() : input;

        Map<Long, CategoryField> active = new LinkedHashMap<>();
        for (CategoryField f : category.getFields()) {
            if (f.isActive()) {
                active.put(f.getId(), f);
            }
        }
        for (Long id : values.keySet()) {
            if (!active.containsKey(id)) {
                throw new BadRequestException("Field " + id + " does not belong to this category");
            }
        }

        Map<CategoryField, String> result = new LinkedHashMap<>();
        for (CategoryField f : active.values()) {
            String raw = values.get(f.getId());
            String value = raw == null ? "" : raw.trim();
            if (value.isEmpty()) {
                if (f.isRequired()) {
                    throw new BadRequestException("Required field missing: " + f.getName());
                }
                continue;
            }
            result.put(f, normalise(f, value));
        }
        return result;
    }

    private static String normalise(CategoryField f, String v) {
        String label = "Field '" + f.getName() + "'";
        try {
            switch (f.getType()) {
                case TEXT:
                    requireMaxLength(label, v, MAX_TEXT);
                    return v;
                case TEXTAREA:
                    requireMaxLength(label, v, MAX_TEXTAREA);
                    return v;
                case NUMBER:
                    return String.valueOf(Long.parseLong(v));
                case DECIMAL:
                    return new BigDecimal(v).toPlainString();
                case BOOLEAN:
                    if (!v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("false")) {
                        throw new BadRequestException(label + " must be true or false");
                    }
                    return v.toLowerCase();
                case DATE:
                    return LocalDate.parse(v).toString();
                case SELECT:
                    if (!CategoryMapper.options(f).contains(v)) {
                        throw new BadRequestException(label + " must be one of " + CategoryMapper.options(f));
                    }
                    return v;
                case MULTI_SELECT:
                    List<String> chosen = List.of(v.split(",")).stream()
                            .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
                    List<String> allowed = CategoryMapper.options(f);
                    if (chosen.isEmpty() || !allowed.containsAll(chosen)) {
                        throw new BadRequestException(label + " must be chosen from " + allowed);
                    }
                    return chosen.stream().collect(Collectors.joining(","));
                default:
                    throw new BadRequestException(label + " has an unsupported type");
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException(label + " must be a valid " +
                    (f.getType() == com.rentivo.backend.category.FieldType.NUMBER ? "whole number" : "number"));
        } catch (DateTimeParseException e) {
            throw new BadRequestException(label + " must be a date in yyyy-MM-dd format");
        }
    }

    private static void requireMaxLength(String label, String v, int max) {
        if (v.length() > max) {
            throw new BadRequestException(label + " must be at most " + max + " characters");
        }
    }
}
