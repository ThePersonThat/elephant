package edu.sumdu.tss.elephant.helper.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class LangTest {

    @ParameterizedTest
    @DisplayName("Should return correct languages")
    @CsvSource(value = {
            "EN, EN",
            "UK, UK",
            "EN, en",
            "UK, uk",
    })
    void testValidValues(Lang expectedLanguage, String language) {
        Lang actualLanguage = Lang.byValue(language);

        assertEquals(expectedLanguage, actualLanguage);
    }

    @ParameterizedTest
    @DisplayName("Should throw runtime exception with correct message")
    @ValueSource(strings = {"FR", "RU"})
    @NullSource
    @EmptySource
    void testInvalidValues(String notExistingLanguage) {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> Lang.byValue(notExistingLanguage));

        assertEquals("Language not found for" + notExistingLanguage, ex.getMessage());
    }
}