package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {
    @Mock Context context;
    @Mock Sql2o sql2o;
    @Mock Connection connection;
    @Mock Query query;
    final String INSERT_LOG_SQL = "INSERT INTO LOGGER(ip, database, \"user\", message) values(:ip, :database, :user, :message);";

    @Test
    void testPush() {
        // prevent static DPool initialization where Keys class with config is required
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            User user = new User();
            user.setLogin("Alex");
            String databaseName = "databaseName";
            String message = "message";
            String ip = "127.0.0.1";

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                // make the getConnection method to return our sql2o
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                // make the sessionAttribute to return our user
                when(context.sessionAttribute(anyString())).thenReturn(user);

                when(sql2o.open()).thenReturn(connection); // make the open method to return our connection
                // make the createQuery method to return our query
                when(connection.createQuery(anyString())).thenReturn(query);

                // make the addParameter method to return our query
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(context.ip()).thenReturn(ip); // make teh ip method to return our ip

                LogService.push(context, databaseName, message);

                // all the methods should be called
                mockedPool.verify(DBPool::getConnection);
                verify(context).sessionAttribute(eq(Keys.SESSION_CURRENT_USER_KEY));
                verify(connection).createQuery(eq(INSERT_LOG_SQL));
                verify(query).addParameter(eq("database"), eq(databaseName));
                verify(query).addParameter(eq("message"), eq(message));
                verify(query).addParameter(eq("user"), eq(user.getLogin()));
                verify(query).addParameter(eq("ip"), eq(ip));
                verify(query).executeUpdate();
            }
        }
    }
}