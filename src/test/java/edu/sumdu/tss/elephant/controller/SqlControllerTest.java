package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqlControllerTest {
    private static IntegrationTestHelper helper;
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
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("SQL console"));
    }

    @Test
    @DisplayName("Should return error if a query is null")
    void testWithEmptyQuery() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .asString();


        assertTrue(response.getBody().contains("<pre class='query'></pre>"));
        assertNotEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Should return error if the query is wrong")
    void testWithWrongQuery() {
        String query = "SELECT * FROM NOT_EXISTING_TABLE;";
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .field("query", query)
                .routeParam("database", database.getName())
                .asString();


        assertTrue(response.getBody().contains("<strong style='color: red;'>"));
        assertNotEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Should perform DDL/DML operations")
    void testDDLandDMLOperations() {
        String createInsertQuery = "CREATE TABLE test (id INTEGER);\n" +
                "INSERT INTO test VALUES (1);\n";

        HttpResponse<String> createInsert = Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .field("query", createInsertQuery)
                .asString();

        assertEquals(200, createInsert.getStatus());
        assertTrue(createInsert.getBody().contains("<pre class='query'>" + createInsertQuery + "</pre>"));
        assertTrue(createInsert.getBody().contains("DDL/DML performed"));

        String updateQuery = "UPDATE test SET id = 2 WHERE id = 1;\n";
        HttpResponse<String> update = Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .field("query", updateQuery)
                .asString();

        assertTrue(update.getBody().contains("<pre class='query'>" + updateQuery + "</pre>"));
        assertTrue(update.getBody().contains(" Changed: 1"));
    }

    @Test
    @DisplayName("Should perform SELECT operations")
    void testSelectOperations() {
        String tableName = "tableTest";
        String createInsertQuery = "CREATE TABLE " + tableName + " (idColumn INTEGER);\n" +
                "INSERT INTO " + tableName + " VALUES (1);\n";


        Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .field("query", createInsertQuery)
                .asString();

        String selectQuery = "SELECT * FROM " + tableName;

        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .field("query", selectQuery)
                .asString();

        assertTrue(response.getBody().contains(tableName));
        assertTrue(response.getBody().contains("idcolumn"));
        assertTrue(response.getBody().contains("<tr><td>1</td><td>1</td></tr>"));
    }
}