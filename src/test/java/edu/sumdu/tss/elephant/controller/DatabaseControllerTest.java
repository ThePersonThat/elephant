package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import edu.sumdu.tss.elephant.model.User;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;
import org.sql2o.Connection;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseControllerTest {
    private static IntegrationTestHelper helper;
    private static String EMAIL;
    private static String PASSWORD;

    @BeforeAll
    static void startServer() {
        helper = new IntegrationTestHelper();

        helper.start();
        helper.clearTables();

        EMAIL = Keys.get("EMAIL.USER");
        PASSWORD = Keys.get("EMAIL.PASSWORD");

        helper.registerUser(EMAIL, PASSWORD);
        helper.loginUser(EMAIL, PASSWORD);
        helper.upgradeUserInCurrentSession(UserRole.BASIC_USER);
    }

    @BeforeEach
    void clearDatabases() {
        try (Connection connection = helper.getSql2o().open()) {
            connection.createQuery("DELETE FROM DATABASES").executeUpdate();
        }
    }

    @AfterAll
    static void stopServer() {
        helper.clearTables();
        helper.stop();
        helper = null;
        Unirest.shutDown();
    }

    @Test
    @DisplayName("Should create database")
    void testCreate() {
        User user = helper.getUserByEmail(EMAIL);
        assertEquals(0, DatabaseService.forUser(user.getUsername()).size());

        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/database").asString();
        assertEquals(1, DatabaseService.forUser(user.getUsername()).size());

        assertEquals(302, response.getStatus());

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") + response.getHeaders()
                .get("Location").get(0)).asString();

        assertTrue(redirect.getBody().contains("Database created"));
    }

    @Test
    @DisplayName("Should return an error if the user database limit is exceeded")
    void testCreateWithExceededLimit() {
        // max db is 2
        Unirest.post(Keys.get("APP.URL") + "/database").asEmpty();
        Unirest.post(Keys.get("APP.URL") + "/database").asEmpty();
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/database").asString();

        assertEquals(302, response.getStatus());

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") + response.getHeaders()
                .get("Location").get(0)).asString();

        assertTrue(redirect.getBody().contains("You limit reached"));
    }

    @Test
    @DisplayName("Should delete database")
    void testDelete() {
        Unirest.post(Keys.get("APP.URL") + "/database").asEmpty();

        User user = helper.getUserByEmail(EMAIL);
        List<Database> databases = DatabaseService.forUser(user.getUsername());
        assertEquals(1, databases.size());

        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/database/{database}/delete")
                .routeParam("database", databases.get(0).getName()).asString();

        assertEquals(0, DatabaseService.forUser(user.getUsername()).size());

        assertEquals(302, response.getStatus());

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") + response.getHeaders()
                .get("Location").get(0)).asString();

        assertTrue(redirect.getBody().contains("Database has been dropped"));
    }
}