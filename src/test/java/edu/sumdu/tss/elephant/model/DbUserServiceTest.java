package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DbUserServiceTest {
    @Mock
    Sql2o sql2o;
    @Mock
    Connection connection;
    @Mock
    Query query;

    private static final ParameterizedStringFactory RESET_USER_SQL = new ParameterizedStringFactory("ALTER USER :name WITH PASSWORD ':password'");
    private static final ParameterizedStringFactory CREATE_USER_SQL = new ParameterizedStringFactory("CREATE USER :name WITH PASSWORD ':password' CONNECTION LIMIT 5 IN ROLE customer;");
    private static final ParameterizedStringFactory DELETE_USER_SQL = new ParameterizedStringFactory("DROP USER :name");

    @Test
    @DisplayName("Should reset DB user password")
    public void testShouldResetDbUserPassword() {
        String username = "test_user";
        String newPassword = "new_12345";
        String querySQL = RESET_USER_SQL.addParameter("name", username).addParameter("password", newPassword).toString();

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                DbUserService.dbUserPasswordReset(username, newPassword);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(querySQL), eq(false));
                verify(query).executeUpdate();

            }

        }
    }

    @Test
    @DisplayName("Should drop user")
    public void testShouldDropUser() {
        String name = "test_user";
        String testPath = "test_path";
        String expectedPath = String.format("sudo remove-user %s",  testPath);

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class);
                 MockedStatic<UserService> mockedUserService = mockStatic(UserService.class);
                 MockedStatic<CmdUtil> mockedCmdUtil = mockStatic(CmdUtil.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.beginTransaction()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);
                mockedUserService.when(() -> UserService.userStoragePath(anyString())).thenReturn(testPath);
                DbUserService.dropUser(name);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(DELETE_USER_SQL.addParameter("name", name).toString()), eq(false));
                verify(query).executeUpdate();
                mockedCmdUtil.verify(() -> CmdUtil.exec(eq(expectedPath)));
            }
        }
    }

    @Test
    @DisplayName("Should init user")
    public void testShouldInitUser() {
        String name = "test_user";
        String password = "test_password";

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                DbUserService.initUser(name, password);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(CREATE_USER_SQL.addParameter("name", name).addParameter("password", password).toString()), eq(false));
                verify(query).executeUpdate();
            }
        }
    }


}