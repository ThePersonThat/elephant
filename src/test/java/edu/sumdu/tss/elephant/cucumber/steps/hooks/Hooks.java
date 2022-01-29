package edu.sumdu.tss.elephant.cucumber.steps.hooks;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.integration.utils.EmailMessageManager;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;

public class Hooks {
    private static IntegrationTestHelper helper;

    @BeforeAll
    public static void initServer() {
        System.out.println("Initialize the server...");
        helper = new IntegrationTestHelper();
        helper.start();
    }

    @AfterAll
    public static void stopServer() {
        helper.stop();
        System.out.println("Server has been stopped...");
    }

    @Before
    public void setUp() {
        clearData();
    }

    @After
    public void close() {
        clearData();
    }

    public static String getPath() {
        return Keys.get("APP.URL");
    }

    public static EmailMessageManager getMailManager() {
        return helper.getEmailManager();
    }

    public void clearData() {
        helper.clearTables();
        helper.clearMailMessages();
    }
}
