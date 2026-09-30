package com.rentivo.backend.common;

import com.rentivo.backend.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumbersTest {

    @Test
    void stripsFormattingAndPlus() {
        assertEquals("919876543210", PhoneNumbers.normalize("+91 98765-43210"));
        assertEquals("9876543210", PhoneNumbers.normalize(" (98765) 43210 "));
    }

    @Test
    void rejectsInvalidNumbers() {
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize(null));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("12345"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("98765abc10"));
        assertThrows(BadRequestException.class, () -> PhoneNumbers.normalize("1234567890123456"));
    }
}
