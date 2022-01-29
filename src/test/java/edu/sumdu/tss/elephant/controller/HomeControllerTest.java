package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeControllerTest {

    private static IntegrationTestHelper helper;

    @BeforeAll
    static void startServer() {
        helper = new IntegrationTestHelper();
        helper.start();
    }

    @AfterAll
    static void stopServer() {
        helper.stop();
        helper = null;
    }

    @BeforeEach
    void clearTables() {
        helper.clearTables();
    }

    @AfterEach
    void shutdown() {
        Unirest.shutDown();
    }

    @Test
    @DisplayName("Should return 200 and show the page")
    void testShow() {
        String email = "alex@jpeg.com";

        // register user to have access
        Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", email)
                .field("password", "alex12H!fdsaf").asString();

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/home").asString();

        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains(email));
    }
}