package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableControllerTest {
    private static IntegrationTestHelper helper;
    public static final String BASIC_PAGE = "/database/{database}/table/";
    private static Database database;
    private static String EMAIL;
    private static String PASSWORD;


    @BeforeAll
    static void startServer() {
        helper = new IntegrationTestHelper();
        helper.start();

        EMAIL = Keys.get("EMAIL.USER");
        PASSWORD = Keys.get("EMAIL.PASSWORD");

        helper.registerUser(EMAIL, PASSWORD);
        helper.loginUser(EMAIL, PASSWORD);

        helper.upgradeUserInCurrentSession(UserRole.BASIC_USER);
        helper.createDatabaseInCurrentSession();
        database = DatabaseService.forUser(helper.getUserByEmail(EMAIL).getUsername()).get(0);
        helper.createTableWithInformationInCurrentSession(database);

    }

    @AfterAll
    static void stopServer() {
        helper.stop();
        helper = null;
    }

    @Test
    @DisplayName("Should test index table")
    void testIndexTable() {

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .asString();
        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("Tables"));

    }

    @Test
    @DisplayName("Should test table preview")
    void testTablePreview() {
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + BASIC_PAGE + "{table}")
                .routeParam("database", database.getName())
                .routeParam("table", "test")
                .asString();
        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("Tables"));
        assertTrue(response.getBody().contains("test"));

    }
}