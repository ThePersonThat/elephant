package edu.sumdu.tss.elephant.middleware;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

class CSRFTokenServiceTest {
    @Test
    @DisplayName("Should return the site wide token")
    void testGetSideWideToken() {
        String expectedToken = "test token";
        String actualToken = CSRFTokenService.getSiteWideToken();

        assertEquals(expectedToken, actualToken);
    }

    /* FIXME:
     * this test does not cover the entire method
     * because there is the false condition (bug) which is not executed
     * if (siteWideToken != null) {
     *      return siteWideToken;
     * }
     * and the siteWideToken is always not null
     */
    @Test
    @DisplayName("Should return the site wide token from env")
    @SetEnvironmentVariable(key = "CSRF_SECRET", value = "Token")
    void testGetSideWideTokenFromEnv() {
        String expectedToken = "token";
        String actualToken = CSRFTokenService.getSiteWideToken();

        assertEquals(expectedToken, actualToken);
    }

    @Test
    @DisplayName("Should generate the token")
    void testGenerateToken() {
        String token = CSRFTokenService.generateToken("someID");
        boolean isBlank = StringUtils.isBlank(token);

        assertFalse(isBlank);
    }

    @Test
    @DisplayName("Should return true if the token is valid")
    void testValidateTokenIfTokenIsValid() {
        String sessionID = "someID";
        String token = CSRFTokenService.generateToken(sessionID);

        boolean actualResult = CSRFTokenService.validateToken(token, sessionID);
        assertTrue(actualResult);
    }

    @Test
    @DisplayName("Should return false if the token is invelid")
    void testValidateTokenIfTokenIsInvalid() {
        String sessionID = "someID";
        String token = CSRFTokenService.generateToken(sessionID + "1234"); // get the token by the different session

        boolean actualResult = CSRFTokenService.validateToken(token, sessionID);
        assertFalse(actualResult);
    }
}