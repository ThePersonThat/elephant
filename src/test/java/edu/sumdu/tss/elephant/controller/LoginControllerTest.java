package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.integration.utils.EmailMessageManager;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.User;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.sql2o.Connection;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {
    private static IntegrationTestHelper helper;
    private static String EMAIL;
    private static String PASSWORD;

    private static String EMAIL_FROM;

    @BeforeAll
    static void startServer() {
        helper = new IntegrationTestHelper();

        helper.start();
        helper.clearTables();

        EMAIL = Keys.get("EMAIL.USER");
        PASSWORD = Keys.get("EMAIL.PASSWORD");
        EMAIL_FROM = Keys.get("EMAIL.FROM");

        helper.registerUser(EMAIL, PASSWORD);

        Unirest.shutDown();
    }

    @AfterAll
    static void stopServer() {
        helper.clearTables();
        helper.stop();
        helper = null;
    }

    @BeforeEach
    void clearMail() {
        helper.clearMailMessages();
    }

    @AfterEach
    void shutdown() {
        Unirest.shutDown();
    }

    @Test
    @DisplayName("Should return 200 and show the page")
    void testShow() {
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/login").asString();

        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
    }

    @Test
    @DisplayName("Should login if user exists")
    void testLoginIfUserExists() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/login")
                .field("login", EMAIL)
                .field("password", PASSWORD).asString();


        assertEquals(302, response.getStatus());
        assertEquals(List.of("/home"), response.getHeaders().get("Location"));

        // perform redirect
        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") + "/home").asString();

        assertTrue(redirect.getBody().contains(EMAIL));
    }

    @Test
    @DisplayName("Should redirect to login page with error if password or login is invalid")
    void testLoginWithInvalidData() {
        HttpResponse<String> postResponse = Unirest.post(Keys.get("APP.URL") + "/login")
                .field("login", "wrongMail")
                .field("password", PASSWORD).asString();

        assertEquals(302, postResponse.getStatus());
        assertEquals(List.of("/login"), postResponse.getHeaders().get("Location"));

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") +
                postResponse.getHeaders().get("Location").get(0)).asString();

        assertTrue(redirect.getBody().contains("User or password not known"));
    }

    @Test
    @DisplayName("Should redirect to login page if user with login does not exist")
    void testLoginIfUserWithLoginDoesNotExists() {
        HttpResponse<String> postResponse = Unirest.post(Keys.get("APP.URL") + "/login")
                .field("login", "wrongMail@gmail.com")
                .field("password", PASSWORD).asString();

        assertEquals(302, postResponse.getStatus());
        assertEquals(List.of("/login"), postResponse.getHeaders().get("Location"));

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") +
                postResponse.getHeaders().get("Location").get(0)).asString();

        assertTrue(redirect.getBody().contains("User or password not known"));
    }

    @Test
    @DisplayName("Should redirect to login page if user with password does not exist")
    void testLoginIfUserWithPasswordDoesNotExists() {
        HttpResponse<String> postResponse = Unirest.post(Keys.get("APP.URL") + "/login")
                .field("login", EMAIL)
                .field("password", "wrongPassword").asString();

        assertEquals(302, postResponse.getStatus());
        assertEquals(List.of("/login"), postResponse.getHeaders().get("Location"));

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") +
                postResponse.getHeaders().get("Location").get(0)).asString();

        assertTrue(redirect.getBody().contains("User or password not known"));
    }

    @Test
    @DisplayName("Should logout")
    void testLogout() {
        Unirest.post(Keys.get("APP.URL") + "/login")
                .field("login", EMAIL)
                .field("password", PASSWORD).asString();

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/logout").asString();
        assertEquals(200, response.getStatus());

        HttpResponse<String> redirect = Unirest.get(Keys.get("APP.URL") + "/login").asString();
        assertEquals(200, redirect.getStatus());
    }

    @Test
    @DisplayName("Should send reset link to the email existing user")
    @SneakyThrows
    void testResetLink() {
        HttpResponse<String> resetGet = Unirest.get(Keys.get("APP.URL") + "/login/reset-password").asString();

        assertEquals(200, resetGet.getStatus());
        assertTrue(resetGet.getBody().contains("Identify yourself"));

        HttpResponse<String> resetPost = Unirest.post(Keys.get("APP.URL") + "/login/reset-password")
                .field("email", EMAIL).asString();

        assertEquals(302, resetPost.getStatus());
        assertEquals(List.of("/login"), resetPost.getHeaders().get("Location"));

        /* checking email */
        MimeMessage[] messages = helper.getMailServer().getReceivedMessagesForDomain(EMAIL);
        MimeMessage message = messages[0];
        assertEquals(1, messages.length);
        assertEquals(EMAIL_FROM, message.getFrom()[0].toString());
        assertEquals("Elephant: Reset password", message.getSubject());
    }

    @Test
    @DisplayName("Should show error if email is invalid")
    @SneakyThrows
    void testResetLinkIfEmailIsInvalid() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/login/reset-password")
                .field("email", "wrongEmail").asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("Is it a valid mail?"));
    }

    @Test
    @DisplayName("Should redirect with error if user does not exists")
    void testResetLinkIfUserDoesNotExists() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/login/reset-password")
                .field("email", "notexisting@email.com").asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("Email not known"));
    }

    @Test
    @DisplayName("Should reset password")
    @SneakyThrows
    void testResetPassword() {
        Unirest.post(Keys.get("APP.URL") + "/login/reset-password")
                .field("email", EMAIL).asString();

        /* getting url reset from the email message */
        EmailMessageManager emailManager = helper.getEmailManager();
        MimeMessage message = emailManager.getCountMessage(EMAIL, 1)[0];
        String url = emailManager.getUrlFromMessage(message);

        HttpResponse<String> resetGet = Unirest.get(Keys.get("APP.URL") + "/login/reset").asString();

        assertEquals(200, resetGet.getStatus());

        // getting current token and password
        String password;
        String token;

        try (Connection connection = helper.getSql2o().open()) {
            List<User> users = connection.createQuery("SELECT * FROM USERS WHERE LOGIN = :login")
                    .addParameter("login", EMAIL).executeAndFetch(User.class);

            assertEquals(1, users.size());
            password = users.get(0).getPassword();
            token = url.substring(url.lastIndexOf("=") + 1);
        }

        HttpResponse<String> resetPost = Unirest.post(url)
                .field("token", token)
                .field("password", PASSWORD + "c").asString();

        assertEquals(302, resetPost.getStatus());
        assertEquals(List.of("/login"), resetPost.getHeaders().get("Location"));

        // should change password and token
        try (Connection connection = helper.getSql2o().open()) {
            List<User> users = connection.createQuery("SELECT * FROM USERS WHERE LOGIN = :login")
                    .addParameter("login", EMAIL).executeAndFetch(User.class);

            assertEquals(1, users.size());
            assertNotEquals(password, users.get(0).getPassword());
            assertNotEquals(token, users.get(0).getToken());
        }
    }

    @Test
    @DisplayName("Should redirect to login page if token does not exist")
    @SneakyThrows
    void testResetPasswordIfTokenDoesNotExist() {
        HttpResponse<String> resetPost = Unirest.post(Keys.get("APP.URL") + "/login/reset")
                .field("token", "wrongToken")
                .field("password", PASSWORD).asString();

        assertEquals(302, resetPost.getStatus());
        assertEquals(List.of("/login"), resetPost.getHeaders().get("Location"));
    }

    @Test
    @DisplayName("Should show error if password is invalid")
    @SneakyThrows
    void testResetPasswordIfItIsInvalid() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/login/reset")
                .field("token", "wrongToken")
                .field("password", "invalidPassword").asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("Password should be at least 8 symbols,  with at least 1 digit, 1 uppercase letter and 1 non alpha-num symbol"));
    }

    @Test
    @DisplayName("Should redirect with new language")
    void testLang() {
        Unirest.get(Keys.get("APP.URL") + "/login").asEmpty();

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/login/lang/{lang}")
                .routeParam("lang", "EN").asString();

        assertEquals(302, response.getStatus());
    }

    @Test
    @DisplayName("Should redirect with error")
    void testLangWithWrongLang() {
        Unirest.get(Keys.get("APP.URL") + "/login").asEmpty();

        String lang = "NoExistingLang";
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/login/lang/{lang}")
                .routeParam("lang", lang).asString();


        assertEquals(302, response.getStatus());
        assertTrue(response.getBody().contains("Language not found for" + lang));
    }
}