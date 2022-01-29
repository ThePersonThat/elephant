package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.User;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sql2o.Connection;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ProfileControllerTest {
    private static IntegrationTestHelper helper;
    private static String EMAIL;
    private static String PASSWORD;

    private static User oldUser;

    @BeforeAll
    static void startServer() {
        helper = new IntegrationTestHelper();

        helper.start();

        EMAIL = Keys.get("EMAIL.USER");
        PASSWORD = Keys.get("EMAIL.PASSWORD");

        helper.registerUser(EMAIL, PASSWORD);
        oldUser = helper.getUserByEmail(EMAIL);
        helper.loginUser(EMAIL, PASSWORD);
    }

    @AfterAll
    static void stopServer() {
        helper.clearTables();
        helper.stop();
        helper = null;
        Unirest.shutDown();
    }


    @Test
    @DisplayName("Should return 200 and show the page")
    void testShow() {
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/profile").asString();

        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
    }

    @Test
    @DisplayName("Should change language for user")
    void testLanguage() {
        String lang = "UK";
        Unirest.get(Keys.get("APP.URL") + "/profile/lang")
                .queryString("lang", lang).asEmpty();

        User updatedUser = helper.getUserByEmail(EMAIL);
        assertEquals(lang, updatedUser.getLanguage());
        assertNotEquals(oldUser.getLanguage(), updatedUser.getLanguage());
    }

    @Test
    @DisplayName("Should reset db password")
    void testResetDbPassword() {
        String dbPassword = "newPassword";
        Unirest.post(Keys.get("APP.URL") + "/profile/reset-db")
                .field("db-password", dbPassword).asEmpty();

        User updatedUser = helper.getUserByEmail(EMAIL);
        assertEquals(dbPassword, updatedUser.getDbPassword());
        assertNotEquals(oldUser.getDbPassword(), updatedUser.getDbPassword());
    }

    @Test
    @DisplayName("Should reset web password")
    void testResetWebPassword() {
        String webPassword = "newPassword";
        Unirest.post(Keys.get("APP.URL") + "/profile/reset-password")
                .field("web-password", webPassword).asEmpty();

        assertNotEquals(oldUser.getPassword(), helper.getUserByEmail(EMAIL).getPassword());
    }

    @Test
    @DisplayName("Should reset api passwords")
    void testResetApiPasswords() {
        Unirest.post(Keys.get("APP.URL") + "/profile/reset-api").asEmpty();

        User updatedUser = helper.getUserByEmail(EMAIL);
        assertNotEquals(oldUser.getPublicKey(), updatedUser.getPublicKey());
        assertNotEquals(oldUser.getPrivateKey(), updatedUser.getPrivateKey());
    }

    @Test
    @DisplayName("Should upgrade user")
    void testUpgradeUser() {
        String role = UserRole.PROMOTED_USER.toString();
        Unirest.post(Keys.get("APP.URL") + "/profile/upgrade")
                .field("role", role).asEmpty();

        User updatedUser = helper.getUserByEmail(EMAIL);
        assertEquals(UserRole.PROMOTED_USER.getValue(), updatedUser.getRole());
        assertNotEquals(oldUser.getRole(), updatedUser.getRole());
    }

    /*
     * this test does not cover the whole the remove method
     * because of a database does not remove user (because it has related objects)
     * and throw an exception
     */
    @Test
    @DisplayName("Remove self")
    void testRemoveSelf() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/profile/remove-self").asString();

        try (Connection connection = helper.getSql2o().open()) {
            List<User> users = connection.createQuery("SELECT * FROM USERS WHERE LOGIN = :login")
                    .addParameter("login", EMAIL).executeAndFetch(User.class);

            assertEquals(0, users.size());
        }
    }
}