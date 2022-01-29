package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessageBundleTest {

    private static MessageBundle messageBundle;

    @BeforeAll
    static void initializeBundleMessage() {
        // should I get access to the private field in order to check locale works correctly?
        messageBundle = new MessageBundle("EN");
    }

    @Test
    @DisplayName("Should initialize messageBundle with selected language")
    void testInitializeBundleMessage() {
        assertNotNull(messageBundle);
    }


    @ParameterizedTest
    @DisplayName("Should return message by key")
    @CsvSource({
            "controls.button.ok, Ok",
            "registration.form.email, Email address",
            "validation.token.empty, Token can't be empty"
    })
    void testGeWithExistingKeys(String key, String expectedValue) {
        String value = messageBundle.get(key);

        assertEquals(expectedValue, value);
    }

    @ParameterizedTest
    @DisplayName("Should return error message if key does not exist")
    @ValueSource(strings = {
            "not.exist.key",
            "not.language.java"
    })
    void testGeWithNotExistingKeys(String key) {
        String value = messageBundle.get(key);
        String expectedString = "I18n not found:" + key;

        assertEquals(expectedString, value);
    }

    // get with params
    // ask the teacher what this method should do
    // I think it should concat a key with params, does it?
    @Test
    @DisplayName("Should return a message with args if key exist")
    void testGeWithExistingKeysWithExtraArgs() {
        String param = "hello";
        String value = messageBundle.get("controls.button.ok", param);
        String expectedValue = "Ok " + param;

        assertEquals(expectedValue, value);
    }
}