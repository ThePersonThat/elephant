package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.CmdUtil;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    Sql2o sql2o;
    @Mock
    Connection connection;
    @Mock
    Query query;

    public static final String USER_BY_LOGIN_SQL = "SELECT * FROM users WHERE login = :login";
    public static final String USER_BY_TOKEN_SQL = "SELECT * FROM users WHERE token = :token";
    private static final String USER_BY_KEY_SQL = "SELECT * FROM users WHERE publicKey = :publicKey";
    private static final String INSERT_SQL =
            "insert into USERS(login, password, role, username, dbPassword, publickey, privatekey, token) " +
                    "values (:login, :password, :role, :username, :dbPassword, :publicKey, :privateKey, :token)";
    private static final String UPDATE_SQL =
            "UPDATE users" +
                    " SET password = :password, role = :role, username= :username, dbPassword=:dbPassword," +
                    " publicKey = :publicKey, privateKey=:privateKey, token=:token, language= :language" +
                    " WHERE id = :id";
    private static final ParameterizedStringFactory CREATE_TABLESPACE_SQL
            = new ParameterizedStringFactory("CREATE TABLESPACE :name" +
            "    OWNER :name" +
            "    LOCATION ':path'");
    private static final ParameterizedStringFactory CHANGE_OWNER
            = new ParameterizedStringFactory("sudo chown -f :user :path");
    private static final ParameterizedStringFactory CHANGE_MODE
            = new ParameterizedStringFactory("chmod 700 :path");
    private static final String[] SPACE_SCOPES = new String[]{"tablespace", "scripts", "backups"};


    @Test
    @DisplayName("Should create default user")
    public void testShouldCreateDefaultUser() {
        User defaultUser = UserService.newDefaultUser();
        assertNotEquals(defaultUser, null);
        assertEquals(defaultUser.getPrivateKey().length(), User.API_KEY_SIZE);
        assertEquals(defaultUser.getPublicKey().length(), User.API_KEY_SIZE);
        assertNotEquals(defaultUser.getToken(), null);
        assertEquals(defaultUser.getUsername().length(), User.USERNAME_SIZE);
        assertEquals(defaultUser.getDbPassword().length(), User.DB_PASSWORD_SIZE);
    }

    @Test
    @DisplayName("Should find user by login")
    public void testShouldFindUserByLogin() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String login = "testLogin";
            User user = new User();
            user.setLogin(login);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(user);

                UserService.byLogin(login);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(USER_BY_LOGIN_SQL));
                verify(query).addParameter(eq("login"), eq(login));
                verify(query).executeAndFetchFirst(same(User.class));
            }
        }
    }

    @Test
    @DisplayName("Should return user by token")
    public void testShouldReturnUserByToken() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String token = "testToken";
            User user = new User();
            user.setToken(token);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(user);

                UserService.byToken(token);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(USER_BY_TOKEN_SQL));
                verify(query).addParameter(eq("token"), eq(token));
                verify(query).executeAndFetchFirst(same(User.class));

            }
        }
    }

    @Test
    @DisplayName("Should return user by token")
    public void testShouldNotReturnUserByToken() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String token = "undefinedToken";

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(null);

                NotFoundException nfe = assertThrows(NotFoundException.class, () -> UserService.byToken(token));

                assertEquals(String.format("User with token %s not found", token), nfe.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Should create (insert) user")
    public void testShouldSaveInsertUser() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            User user = new User();
            Long testId = 10L;

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.bind(user)).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);
                when(connection.getKey(Long.class)).thenReturn(testId);

                UserService.save(user);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(INSERT_SQL));
                verify(query).bind(eq(user));
                verify(query).executeUpdate();
                assertEquals(user.getId(), testId);
            }
        }
    }

    @Test
    @DisplayName("Should throw exception at the time of user creation")
    public void testShouldThrowExceptionSaveInsertUser() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            User user = new User();

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenThrow(RuntimeException.class);

                assertThrows(Exception.class, () -> UserService.save(user));
            }
        }
    }

    @Test
    @DisplayName("Should create (update) user")
    public void testShouldSaveUpdateUser() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            User user = new User();
            user.setId(999L);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.bind(user)).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);
                when(connection.getKey(Long.class)).thenReturn(user.getId());

                UserService.save(user);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(UPDATE_SQL));
                verify(query).bind(eq(user));
                verify(query).executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("Should return user storage path")
    public void testShouldReturnUserStoragePath() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            String testValue = "testValue";
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn(testValue);

            String testOwner = "testOwner";

            String userStoragePath = UserService.userStoragePath(testOwner);

            assertEquals(testValue + testOwner, userStoragePath);
        }
    }

    @Test
    @DisplayName("Should return user by public key")
    public void testShouldReturnUserByPublicKey() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            User user = new User();
            user.setPublicKey("test public key");

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(user);

                UserService.byPublicKey(user.getPublicKey());

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(USER_BY_KEY_SQL));
                verify(query).addParameter(eq("publicKey"), eq(user.getPublicKey()));
                verify(query).executeAndFetchFirst(same(User.class));

            }
        }
    }

    @Test
    @DisplayName("Should create tablespace")
    public void testShouldCreateTableSpace() {
        String testOwner = "testOwner";
        String testPath = "testPath";
        String testCreateTablespaceQuery = CREATE_TABLESPACE_SQL.addParameter("name", testOwner).addParameter("path", testPath.replace("\\", "\\\\")).toString();
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                UserService.createTablespace(testOwner, testPath);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(testCreateTablespaceQuery), eq(false));
                verify(query).executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("Should return size of user storage")
    public void testShouldReturnUserStorageSize() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            String testValue = "test_dir_";
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn(testValue);
            String testOwner = "owner";
            String userStoragePath = UserService.userStoragePath(testOwner);
            File tmpFile = new File(userStoragePath);
            tmpFile.mkdir();

            File userDir = new File(userStoragePath);

            long userStorageSizeExp = FileUtils.sizeOfDirectory(userDir);

            long userStorageAct = UserService.storageSize(testOwner);

            assertEquals(userStorageSizeExp, userStorageAct);
        }
    }

    @Test
    @DisplayName("Should throw exception at the time of getting user storage size")
    public void testShouldThrowExceptionInStorageSize() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("anyString");
            try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
                fileUtils.when(() -> FileUtils.sizeOfDirectory(any())).thenThrow(IllegalArgumentException.class);
                assertEquals(0, UserService.storageSize("test"));
            }
        }
    }


    /**
     * This test doesn't cover if (scope.equals("tablespace") && !SystemUtils.IS_OS_WINDOWS)  block
     * on Windows environment, but covers it on Linux or another environment
     * Explanation: we can not test Linux specific commands on Windows environment.
     */

    @Test
    @DisplayName("Should init user storage")
    public void testInitUserStorage() {
        String owner = "test_owner";

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
             MockedStatic<CmdUtil> mockedCmdUtil = mockStatic(CmdUtil.class);
             MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            String testValue = "testValue";
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn(testValue);

            UserService.initUserStorage(owner);
            if (!SystemUtils.IS_OS_WINDOWS) {
                mockedCmdUtil.verify(() -> CmdUtil.exec(anyString()));
            }
            mockedFileUtils.verify(() -> FileUtils.forceMkdir(any()), times(3));
        }
    }

    @Test
    @DisplayName("Should throws exception in init user storage")
    public void testInitUserStorageShouldThrowException() {
        String owner = "test_owner";

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
             MockedStatic<FileUtils> mockedFileUtils = mockStatic(FileUtils.class)) {
            String testValue = "testValue";
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn(testValue);
            mockedFileUtils.when(() -> FileUtils.forceMkdir(any())).thenThrow(RuntimeException.class);

            assertThrows(RuntimeException.class, () -> UserService.initUserStorage(owner));
        }
    }

    @Test
    @DisplayName("Should throw an exception if user is not found")
    void testByLoginIfUserIsNotFound() {
        String login = "test";
        String expectedMessage = String.format("User with mail %s not found", login);

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("anyString");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(User.class)).thenReturn(null);

                NotFoundException e = assertThrows(NotFoundException.class, () -> UserService.byLogin(login));
                assertEquals(expectedMessage, e.getMessage());
            }
        }
    }
}
