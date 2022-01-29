package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.Backup;
import edu.sumdu.tss.elephant.model.BackupService;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;
import org.sql2o.Connection;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class BackupControllerTest {
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
        helper.createTableWithInformationInCurrentSession(database);
    }

    @AfterEach
    void clearBackups() {
        try (Connection connection = helper.getSql2o().open()) {
            connection.createQuery("DELETE FROM BACKUPS").executeUpdate();
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
    @DisplayName("Should create backup from post parameter")
    void testCreateBackupFromPost() {
        String pointName = "point1";

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .routeParam("database", database.getName())
                .field("point", pointName).asString();

        List<Backup> backups = BackupService.list(database.getName());
        assertEquals(1, backups.size());
        assertEquals(database.getName(), backups.get(0).getDatabase());
        assertEquals(pointName, backups.get(0).getPoint());

        assertTrue(new File(BackupService.filePath(database.getOwner(), database.getName(), pointName)).exists());
    }

    @Test
    @DisplayName("Should create backup from path parameter")
    void testCreateBackupFromPath() {
        String pointName = "point2";

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point/{point}/create")
                .routeParam("database", database.getName())
                .routeParam("point", pointName).asString();

        List<Backup> backups = BackupService.list(database.getName());
        assertEquals(1, backups.size());
        assertEquals(database.getName(), backups.get(0).getDatabase());
        assertEquals(pointName, backups.get(0).getPoint());

        assertTrue(new File(BackupService.filePath(database.getOwner(), database.getName(), pointName)).exists());
    }

    @Test
    @DisplayName("Should return the error if point name is null")
    void testCreateBackupWithEmptyPointName() {
        HttpResponse<String> postResponse = Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .field("point", "")
                .routeParam("database", database.getName()).asString();

        assertEquals(302, postResponse.getStatus());

        HttpResponse<String> getResponse = Unirest.get(Keys.get("APP.URL") + postResponse.getHeaders()
                .get("Location").get(0)).asString();

        assertTrue(getResponse.getBody().contains("Point name can't be empty"));
    }

    @Test
    @DisplayName("Should return the error if backup limit is exceeded")
    void testCreateWithExceededLimit() {
        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .field("point", StringUtils.randomAlphaString(6))
                .routeParam("database", database.getName()).asString();

        HttpResponse<String> postResponse = Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .field("point", StringUtils.randomAlphaString(6))
                .routeParam("database", database.getName()).asString();

        assertEquals(302, postResponse.getStatus());

        HttpResponse<String> getResponse = Unirest.get(Keys.get("APP.URL") + postResponse.getHeaders()
                .get("Location").get(0)).asString();

        assertTrue(getResponse.getBody().contains("You limit reached"));
    }

    @Test
    @DisplayName("Should delete backup")
    void testDeleteBackup() {
        String pointName = "point1";

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .field("point", pointName)
                .routeParam("database", database.getName()).asString();

        assertEquals(1, BackupService.list(database.getName()).size());
        assertTrue(new File(BackupService.filePath(database.getOwner(), database.getName(), pointName)).exists());

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point/{point}/delete")
                .routeParam("point", pointName)
                .routeParam("database", database.getName()).asString();

        assertEquals(0, BackupService.list(database.getName()).size());
        assertFalse(new File(BackupService.filePath(database.getOwner(), database.getName(), pointName)).exists());
    }

    @Test
    @DisplayName("Should restore database from backup")
    void testRestoreBackup() {
        String pointName = "point1";

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .field("point", pointName)
                .routeParam("database", database.getName()).asString();

        // change the database
        Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .field("query", "INSERT INTO test VALUES (2)")
                .asString();

        try (Connection connection = DBPool.getConnection(database.getName()).open()) {
            List<String> strings = connection.createQuery("SELECT * FROM test").executeAndFetch(String.class);
            assertEquals(2, strings.size());
        }

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point/{point}/reset")
                .routeParam("point", pointName)
                .routeParam("database", database.getName()).asString();

        try (Connection connection = DBPool.getConnection(database.getName()).open()) {
            List<String> restoredTable = connection.createQuery("SELECT * FROM test").executeAndFetch(String.class);

            assertEquals(1, restoredTable.size());
            assertEquals("1", restoredTable.get(0));
        }
    }

    @Test
    @DisplayName("Should show the backup list")
    void testIndex() {
        String pointName = "point1";

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/point")
                .field("point", pointName)
                .routeParam("database", database.getName()).asString();

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/database/{database}/point/")
                .routeParam("database", database.getName()).asString();

        assertEquals(200 ,response.getStatus());
        assertTrue(response.getBody().contains(pointName));
    }
}