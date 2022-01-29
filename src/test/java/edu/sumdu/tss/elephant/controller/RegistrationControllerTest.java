package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.enums.Lang;
import edu.sumdu.tss.elephant.integration.utils.EmailMessageManager;
import edu.sumdu.tss.elephant.integration.utils.IntegrationTestHelper;
import edu.sumdu.tss.elephant.model.User;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.StringUtils;
import org.sql2o.Connection;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationControllerTest {
    private static String EMAIL;
    private static String PASSWORD;

    private static String EMAIL_FROM;
    private static IntegrationTestHelper helper;

    @BeforeAll
    @SneakyThrows
    static void startServer() {
        helper = new IntegrationTestHelper();

        EMAIL = Keys.get("EMAIL.USER");
        PASSWORD = Keys.get("EMAIL.PASSWORD");
        EMAIL_FROM = Keys.get("EMAIL.FROM");

        helper.start();
    }

    @AfterAll
    @SneakyThrows
    static void stopServer() {
        helper.stop();
        helper = null;
    }

    @BeforeEach
    void clearDatabaseAndMails() {
        helper.clearTables();
        helper.clearMailMessages();
    }

    @AfterEach
    void shutdown() {
        Unirest.shutDown();
    }


    @Test
    @DisplayName("Should return code 200")
    void testShow() {
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/registration").asString();

        assertEquals(200, response.getStatus());
        assertEquals(List.of("text/html"), response.getHeaders().get("Content-Type"));
        assertTrue(StringUtils.isNotBlank(response.getBody()));
    }

    @Test
    @DisplayName("Should register a new user with creation all the data it needs")
    @SneakyThrows
    void testRegisterNewUser() {
        HttpResponse<String> response = Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", EMAIL)
                .field("password", PASSWORD).asString();


        // 302 redirect
        assertEquals(302, response.getStatus());
        assertEquals(List.of("/home"), response.getHeaders().get("Location"));

        /* checking email */
        EmailMessageManager emailManager = helper.getEmailManager();
        MimeMessage message = emailManager.getCountMessage(EMAIL, 1)[0];
        emailManager.checkMessage(message, "Elephant: Welcome to the club buddy", EMAIL_FROM);

        try (Connection connection = helper.getSql2o().open()) {
            /* checking user creation */
            List<User> users = connection.createQuery("SELECT * FROM USERS")
                    .executeAndFetch(User.class);

            assertEquals(1, users.size());
            User user = users.get(0);
            assertEquals(EMAIL, user.getLogin());
            assertTrue(StringUtils.isNotBlank(user.getPassword()));
            assertTrue(StringUtils.isNotBlank(user.getUsername()));
            assertTrue(StringUtils.isNotBlank(user.getDbPassword()));
            assertTrue(StringUtils.isNotBlank(user.getToken()));
            assertEquals(UserRole.UNCHEKED.getValue(), user.getRole());
            assertTrue(StringUtils.isNotBlank(user.getPublicKey()));
            assertTrue(StringUtils.isNotBlank(user.getPrivateKey()));
            assertEquals(Lang.EN.toString(), user.getLanguage());

            /* checking database user creation */
            List<String> dbUsers = connection.createQuery("SELECT usename FROM pg_catalog.pg_user WHERE usename = :name")
                    .addParameter("name", user.getUsername())
                    .executeAndFetch(String.class);

            assertEquals(1, dbUsers.size());


            /* checking folders creation */
            String path = Keys.get("DB.LOCAL_PATH") + File.separator + user.getUsername();

            assertTrue(Files.exists(Paths.get(path)));
            assertTrue(Files.exists(Paths.get(path + File.separator + "tablespace")));
            assertTrue(Files.exists(Paths.get(path + File.separator + "scripts")));
            assertTrue(Files.exists(Paths.get(path + File.separator + "backups")));


            /* checking tablespace creation */
            List<String> tableSpace = connection.createQuery("SELECT spcname FROM pg_tablespace WHERE spcname = :name")
                    .addParameter("name", user.getUsername())
                    .executeAndFetch(String.class);

            assertEquals(1, tableSpace.size());
        }
    }

    @Test
    @DisplayName("Should prevent registration if the user already exists")
    @SneakyThrows
    void testRegisterUserIfItExists() {
        Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", EMAIL)
                .field("password", PASSWORD).asEmpty();

        Unirest.shutDown();

        // the second request with the same data
        HttpResponse<String> postResponse = Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", EMAIL)
                .field("password", PASSWORD).asString();

        // redirect
        assertEquals(302, postResponse.getStatus());
        assertEquals(List.of("/registration"), postResponse.getHeaders().get("Location"));

        // processing redirect
        HttpResponse<String> getResponse = Unirest.get(Keys.get("APP.URL") + "/registration").asString();

        assertTrue(getResponse.getBody().contains("Login (email) already taken"));
    }

    /* FIXME:
     * this test does not cover the entire method
     * because there is throwing wrong exception (bug) which prevents executing right one
     * UserService.byToken
     * if (user == null) {
     *           throw new NotFoundException(String.format("User with token %s not found", token));
     * }
     * Prevent executing:
     * User user = UserService.byToken(token);
     * if (user == null) {
     *       throw new NotFoundException("User with this token not found (or token was renewed)");
     * }
     */
    @Test
    @DisplayName("Should return 404 if user is not found by token")
    void testConfirmationWithNotExistingUser() {
        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/registration/confirm/{token}")
                .routeParam("token", "dummyToken").asString();

        assertEquals(404, response.getStatus());
        assertTrue(response.getBody().contains("User with this token not found (or token was renewed)"));
    }

    @Test
    @DisplayName("Should confirm the token")
    @SneakyThrows
    void testConfirmationToken() {
        Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", EMAIL)
                .field("password", PASSWORD).asEmpty();

        /* getting url confirmation from the email message */
        EmailMessageManager emailManager = helper.getEmailManager();

        MimeMessage message = emailManager.getCountMessage(EMAIL, 1)[0];
        EmailMessageManager.UrlWithToken urlToken = emailManager.getUrlWithTokenFromMessage(message);

        /* checking confirmation */
        HttpResponse<String> response = Unirest.get(urlToken.getUrl())
                .routeParam("token", urlToken.getToken()).asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("Email approved"));
    }

    @Test
    @DisplayName("Should resend confirmation token")
    @SneakyThrows
    void testResendConfirmationToken() {
        Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", EMAIL)
                .field("password", PASSWORD).asEmpty();

        HttpResponse<String> response = Unirest.get(Keys.get("APP.URL") + "/registration/resend-confirm")
                .asString();

        assertTrue(response.getBody().contains("Resend conformation email"));

        EmailMessageManager emailManager = helper.getEmailManager();
        MimeMessage message = emailManager.getCountMessage(EMAIL, 2)[1];
        emailManager.checkMessage(message, "Elephant: Welcome to the club buddy", EMAIL_FROM);
    }
}