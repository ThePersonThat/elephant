package edu.sumdu.tss.elephant.integration.utils;

import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import edu.sumdu.tss.elephant.Server;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.MailService;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.User;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.Security;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationTestHelper {
    private Sql2o sql2o;
    private Server server;
    private GreenMail mailServer;
    private EmailMessageManager emailManager;
    private static final String CONFIG_NAME = "config.properties";

    @SneakyThrows
    public IntegrationTestHelper() {
        Keys.loadParams(new File(CONFIG_NAME));

        this.sql2o = new Sql2o("jdbc:postgresql://" + Keys.get("DB.HOST") + ":"
                + Keys.get("DB.PORT") + "/" + Keys.get("DB.NAME"), Keys.get("DB.USERNAME"), Keys.get("DB.PASSWORD"));
        this.server = new Server();
        buildMailServer();
    }

    public void start() {
        server.start(Integer.parseInt(Keys.get("APP.PORT")));

        startMailServer();
    }

    public void stop() {
        server.stop();
        stopMailServer();
    }


    public GreenMail getMailServer() {
        return mailServer;
    }

    @SneakyThrows
    public void clearMailMessages() {
        mailServer.purgeEmailFromAllMailboxes();
    }

    public Sql2o getSql2o() {

        return sql2o;
    }

    public void clearTables() {
        try (Connection connection = sql2o.open()) {
            connection.createQuery("DELETE FROM BACKUPS").executeUpdate();
            connection.createQuery("DELETE FROM DATABASES").executeUpdate();
            connection.createQuery("DELETE FROM LOGGER").executeUpdate();
            connection.createQuery("DELETE FROM SCRIPTS").executeUpdate();
            connection.createQuery("DELETE FROM USERS").executeUpdate();
        }
    }

    public User getUserByEmail(String email) {

        try (Connection connection = sql2o.open()) {
            List<User> users = connection.createQuery("SELECT * FROM USERS WHERE LOGIN = :login")
                    .addParameter("login", email).executeAndFetch(User.class);

            assertEquals(1, users.size());

            return users.get(0);
        }
    }

    public void registerUser(String login, String password) {
        Unirest.post(Keys.get("APP.URL") + "/registration")
                .field("login", login)
                .field("password", password).asEmpty();
    }

    public void loginUser(String login, String password) {
        Unirest.post(Keys.get("APP.URL") + "/login")
                .field("login", login)
                .field("password", password).asEmpty();
    }

    public void upgradeUserInCurrentSession(UserRole role) {
        Unirest.post(Keys.get("APP.URL") + "/profile/upgrade")
                .field("role", role.toString()).asEmpty();
    }

    public void createTableWithInformationInCurrentSession(Database database) {
        String createInsertQuery = "CREATE TABLE test (id INTEGER);\n" +
                "INSERT INTO test VALUES (1);\n";

        Unirest.post(Keys.get("APP.URL") + "/database/{database}/sql")
                .routeParam("database", database.getName())
                .field("query", createInsertQuery)
                .asString();
    }

    public void createDatabaseInCurrentSession() {
        Unirest.post(Keys.get("APP.URL") + "/database").asString();
    }

    @SneakyThrows
    private void startMailServer() {
        /*
            change the mail service with new configuration, because of this line:
            private static final MailService mail = new MailService();
         */
        Constructor<?> constructor = MailService.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        Field unsafeFiled = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeFiled.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeFiled.get(null);

        Field mail = MailService.class.getDeclaredField("mail");
        Object o = unsafe.staticFieldBase(mail);
        long offset = unsafe.staticFieldOffset(mail);
        unsafe.putObject(o, offset, constructor.newInstance());

        mailServer.start();
    }

    private void stopMailServer() {
        mailServer.stop();
    }

    private void buildMailServer() {
        Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());

        this.mailServer = new GreenMail(new ServerSetup(Integer.parseInt(Keys.get("EMAIL.PORT")), Keys.get("EMAIL.HOST"), ServerSetup.PROTOCOL_SMTPS));
        this.mailServer.setUser(Keys.get("EMAIL.USER"), Keys.get("EMAIL.PASSWORD"));
        this.emailManager = new EmailMessageManager(mailServer);
    }

    public EmailMessageManager getEmailManager() {
        return emailManager;
    }
}
