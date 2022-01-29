package edu.sumdu.tss.elephant.helper.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * password requirements:
 *      more than 8 chars,
 *      at least 1 number,
 *      1 upper case letter,
 *      1 specific symbol
 *
 * email requirements:
 *      at least one "@" symbol,
 *      one "." symbol
 */
class ValidatorHelperTest {

    @Test
    @DisplayName("Should return true if the password is valid")
    void testValidPassword() {
        String validPassword = "alexIH-83-0";

        assertTrue(ValidatorHelper.isValidPassword(validPassword));
    }

    @ParameterizedTest
    @DisplayName("Should return false if the password is invalid")
    @EmptySource
    @NullSource
    @ValueSource(strings = {
            "alexI6!", // less than 8 chars
            "alexI!eag", // without number
            "alex65ea!", // no uppercase letter
            "alex5356Hreq", // without specific symbol
    })
    void testInvalidPasswords(String invalidPassword) {
        assertFalse(ValidatorHelper.isValidPassword(invalidPassword));
    }

    @Test
    @DisplayName("Should return true if the email is valid")
    void testValidEmail() {
        String validEmail = "alex@gmail.com";

        assertTrue(ValidatorHelper.isValidMail(validEmail));
    }

    @ParameterizedTest
    @DisplayName("Should return false if the email is invalid")
    @EmptySource
    @NullSource
    @ValueSource(strings = {
            "alex.gmail.com", // without '@' symbol
            "alex@gmailcom", // without '.' symbol
            "alex.@jpegcom" // the '.' symbol at the wrong place
    })
    void testInvalidEmails(String invalidEmail) {
        assertFalse(ValidatorHelper.isValidMail(invalidEmail));
    }
}