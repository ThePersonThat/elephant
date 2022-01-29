package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.*;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import org.junit.jupiter.api.*;
import org.sql2o.Connection;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptControllerTest {
    private static IntegrationTestHelper helper;
    public static final String BASIC_PAGE = "/database/{database}/script/";
    private static Database database;
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
        helper.createDatabaseInCurrentSession();
        database = DatabaseService.forUser(helper.getUserByEmail(EMAIL).getUsername()).get(0);
        helper.createTableWithInformationInCurrentSession(database);

    }

    @AfterAll
    static void stopServer() {
        helper.clearTables();
        helper.stop();
        helper = null;
        Unirest.shutDown();
    }

    @BeforeEach
    void clearDatabases() {
        try (Connection connection = helper.getSql2o().open()) {
            connection.createQuery("DELETE FROM SCRIPTS").executeUpdate();
        }
    }


    @Test
    @DisplayName("Should test index script")
    void testIndexScript() {
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .asString();
        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("Scripts"));
    }

    @Test
    @DisplayName("Should test delete script")
    void testDeleteScript() throws IOException {
        File scrFile = new File("script.test");
        scrFile.createNewFile();
        String scriptName = scrFile.getName();

        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        List<Script> scripts = ScriptService.list(database.getName());
        assertEquals(1, scripts.size());
        assertEquals(database.getName(), scripts.get(0).getDatabase());
        assertEquals(scriptName, scripts.get(0).getFilename());

        long scriptId = ScriptService.list(database.getName()).get(0).getId();

        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE + "{script}/delete")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();



        assertEquals(0, ScriptService.list(database.getName()).size());
    }

    @Test
    @DisplayName("Should test script creating")
    void testScriptCreating() throws IOException {
        String testScriptLines =
                "CREATE TABLE test (id INTEGER);\n" +
                        "INSERT INTO test VALUES (99);\n" +
                        "SELECT * FROM test";

        File scrFile = new File("script.test");
        scrFile.createNewFile();
        Writer fileWriter = new FileWriter(scrFile);
        fileWriter.write(testScriptLines);
        fileWriter.close();

        BufferedReader brInitialScript = new BufferedReader(new FileReader(scrFile.getPath()));
        StringBuilder sbInitialScript = new StringBuilder();
        String lineOfInitialScript;
        while ((lineOfInitialScript = brInitialScript.readLine()) != null) {
            sbInitialScript.append(lineOfInitialScript);
        }

        String scriptName = scrFile.getName();
        int initSize = ScriptService.list(database.getName()).size();
        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        List<Script> scripts = ScriptService.list(database.getName());
        assertEquals(initSize + 1, scripts.size());
        assertEquals(database.getName(), scripts.get(0).getDatabase());
        assertEquals(scriptName, scripts.get(0).getFilename());

        File destinationFile = new File(scripts.get(0).getPath());
        BufferedReader brCreatedScript = new BufferedReader(new FileReader(destinationFile.getPath()));
        StringBuilder sbCreatedScript = new StringBuilder();
        String lineOfCreatedScript;

        while ((lineOfCreatedScript = brCreatedScript.readLine()) != null) {
            sbCreatedScript.append(lineOfCreatedScript);
        }

        assertEquals(sbInitialScript.toString(), sbCreatedScript.toString());

        long scriptId = ScriptService.list(database.getName()).get(0).getId();

        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE + "{script}/delete")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();
    }

    @Test
    @DisplayName("Shouldn't create new script (max scripts constraint)")
    void testMaxScriptsConstraint() throws IOException {
        helper.upgradeUserInCurrentSession(UserRole.UNCHEKED);
        File scrFile = new File("script.test");
        scrFile.createNewFile();
        int initSize = ScriptService.list(database.getName()).size();

        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        List<Script> scripts = ScriptService.list(database.getName());
        assertEquals(initSize, scripts.size());

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") + response.getHeaders()
                .get("Location").get(0)).asString();
        assertTrue(redirect.getBody().contains("You limit reached"));

        helper.upgradeUserInCurrentSession(UserRole.BASIC_USER);
    }

    @Test
    @DisplayName("Should test script indexing")
    void testScriptIndexing() throws IOException {
        File scrFile = new File("script.test");
        scrFile.createNewFile();
        String scriptName = scrFile.getName();
        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        List<Script> scripts = ScriptService.list(database.getName());
        assertEquals(1, scripts.size());
        assertEquals(database.getName(), scripts.get(0).getDatabase());
        assertEquals(scriptName, scripts.get(0).getFilename());

        long scriptId = ScriptService.list(database.getName()).get(0).getId();
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + BASIC_PAGE + "{script}")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();

        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("Should test script running")
    void testShouldRunScript() throws IOException {
        String testScriptLines =
                "CREATE TABLE test (id INTEGER);\n" +
                "INSERT INTO test VALUES (99);\n" +
                "SELECT * FROM test";

        File scrFile = new File("script.test");
        scrFile.createNewFile();
        Writer fileWriter = new FileWriter(scrFile);
        fileWriter.write(testScriptLines);
        fileWriter.close();

        String scriptName = scrFile.getName();
        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        List<Script> scripts = ScriptService.list(database.getName());
        assertEquals(1, scripts.size());
        assertEquals(database.getName(), scripts.get(0).getDatabase());
        assertEquals(scriptName, scripts.get(0).getFilename());

        long scriptId = ScriptService.list(database.getName()).get(0).getId();
        Script s = ScriptService.list(database.getName()).get(0);

        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + BASIC_PAGE + "{script}")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("ok"));
        assertTrue(response.getBody().contains("020000З"));

    }

    /**
     *  This test is not included in the coverage of tests,
     *  since this is a HTTP error and it is not caught as an execution,
     *  but this case was checked for case when the file with the script was deleted.
     *  */
    @Test
    @DisplayName("Should test throwing HTTP500 exception at the time of running")
    void testShouldNotRunScript() throws IOException {
        File scrFile = new File("script.test");
        scrFile.createNewFile();

        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        Script createdScript = ScriptService.list(database.getName()).get(0);
        File createdTmpFile = new File(createdScript.getPath());
        createdTmpFile.delete();

        long scriptId = createdScript.getId();

        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + BASIC_PAGE + "{script}")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();

        assertEquals(500, response.getStatus());

    }

    @Test
    @DisplayName("Should test script showing")
    void testScriptShowing() throws IOException {
        String testScriptLines =
                "CREATE TABLE test (id INTEGER);\n" +
                        "INSERT INTO test VALUES (99);\n" +
                        "SELECT * FROM test";

        File scrFile = new File("script.test");
        scrFile.createNewFile();
        Writer fileWriter = new FileWriter(scrFile);
        fileWriter.write(testScriptLines);
        fileWriter.close();

        BufferedReader brInitialScript = new BufferedReader(new FileReader(scrFile.getPath()));
        StringBuilder sbInitialScript = new StringBuilder();
        String lineOfInitialScript;
        while ((lineOfInitialScript = brInitialScript.readLine()) != null) {
            sbInitialScript.append(lineOfInitialScript);
        }

        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE)
                .routeParam("database", database.getName())
                .field("file", scrFile).asString();

        long scriptId = ScriptService.list(database.getName()).get(0).getId();

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + BASIC_PAGE+ "{script}")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();
        assertTrue(response.getBody().contains(testScriptLines));

        Unirest.post(Keys.get("APP.URL") + BASIC_PAGE + "{script}/delete")
                .routeParam("database", database.getName())
                .routeParam("script", String.valueOf(scriptId)).asString();
    }


}