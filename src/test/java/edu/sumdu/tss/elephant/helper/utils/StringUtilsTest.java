package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StringUtilsTest {

    @ParameterizedTest
    @DisplayName("Should generate a random alpha string with a given length")
    @ValueSource(ints = {5, 10, 40})
    void testRandomAlphaString(int length) {
        String alphaString = StringUtils.randomAlphaString(length);
        boolean isBlank = org.junit.platform.commons.util.StringUtils.isBlank(alphaString);

        assertFalse(isBlank);
        assertEquals(length, alphaString.length());
    }

    @Test
    @DisplayName("Should generate a random uuid string")
    void testUUID() {
        String uuid = StringUtils.uuid();
        boolean isBlank = org.junit.platform.commons.util.StringUtils.isBlank(uuid);

        assertFalse(isBlank);
        assertEquals(36, uuid.length()); // 32 is length of an uuid string
    }

    @Test
    @DisplayName("Should insert a text after existing substring")
    void testReplaceLastWithExistingSubString() {
        String text = "Hello world";
        String textToInsert = " dear ";
        String expectedString = "Hello dear world";
        String result = StringUtils.replaceLast(text, " ", textToInsert);

        assertEquals(expectedString, result);
    }

    @Test
    @DisplayName("Should not insert a text because of a substring does not exist")
    void testReplaceLastWithNotExistingSubString() {
        String text = "Hello world";
        String textToInsert = " dear ";
        String result = StringUtils.replaceLast(text, "bye", textToInsert);

        assertEquals(text, result);
    }

}