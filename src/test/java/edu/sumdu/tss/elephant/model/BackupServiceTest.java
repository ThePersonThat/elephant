package edu.sumdu.tss.elephant.model;


import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
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
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** The class has too many dependencies to test.
 * In particular, methods restore() and perform() have too many dependencies.
 * We couldn't test other methods because they are private and static and we can test them only as part of specified above methods.
 * Untested methods of this class are better to test as part of Integration Testing
 * */


@ExtendWith(MockitoExtension.class)
public class BackupServiceTest {
    @Mock
    Sql2o sql2o;
    @Mock
    Connection connection;
    @Mock
    Query query;

    private static final String LIST_BACKUP_SQL = "SELECT * from backups where database = :database";
    private static final String GET_BY_NAME_SQL = "SELECT * from backups where database = :database and point = :point";
    private static final String INSERT_SQL =
            """
                    insert into backups(database, point, status, "createdAt", "updatedAt")
                    values (:database, :point, :status, :createdAt, :updatedAt)  ON CONFLICT(database, point) DO UPDATE\s
                    SET status = excluded.status,\s
                       "updatedAt" = now();""";
    private static final String DELETE_BACKUP = "DELETE FROM backups WHERE database = :database and point = :point;";


    @Test
    @DisplayName("Should find list of backups")
    public void testShouldFindListOfBackups() {
        String dbName = "test_db";
        Backup backup1 = new Backup();
        backup1.setDatabase(dbName);
        Backup backup2 = new Backup();
        backup2.setDatabase(dbName);
        List<Backup> backups = List.of(backup1, backup2);

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetch(any(Class.class))).thenReturn(backups);

                BackupService.list(dbName);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(LIST_BACKUP_SQL));
                verify(query).addParameter(eq("database"), eq(dbName));
                verify(query).executeAndFetch(same(Backup.class));
            }
        }
    }

    @Test
    @DisplayName("Should find Backup by DB Name and Point")
    public void testShouldFindBackupByDBNameAndPoint() {
        String dbName = "test_db";
        String point = "test_point";
        Backup backup = new Backup();
        backup.setDatabase(dbName);
        backup.setPoint(point);

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(backup);

                BackupService.byName(dbName, point);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(GET_BY_NAME_SQL));
                verify(query).addParameter(eq("database"), eq(dbName));
                verify(query).addParameter(eq("point"), eq(point));
                verify(query).executeAndFetchFirst(same(Backup.class));
            }
        }
    }

    @Test
    @DisplayName("Shouldn't find Backup by DB Name and Point and throw NotFoundException")
    public void testShouldNotFindBackupByDBNameAndPoint() {
        String dbName = "test_db";
        String point = "test_point";

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(null);


                NotFoundException nfe = assertThrows(NotFoundException.class, () -> BackupService.byName(dbName, point));
                assertEquals("Point not found", nfe.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Should return file path")
    public void testShouldReturnFilePath() {
        String userStoragePath = "test_user_path";
        String database = "test_db";
        String point = "test_point";
        String owner = "test_owner";
        String expectedPath = userStoragePath +   File.separator + "backups" + File.separator + database +  File.separator + point;

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
             MockedStatic<UserService> mockedUserService = mockStatic(UserService.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            mockedUserService.when(() -> UserService.userStoragePath(anyString())).thenReturn(userStoragePath);
            String actualPath = BackupService.filePath(owner, database, point);
            assertEquals(expectedPath, actualPath);
        }
    }

    @Test
    @DisplayName("Should delete Backup")
    public void testShouldDeleteBackup() throws IOException {
        String userStoragePath = "test_user_path";
        String database = "test_db";
        String point = "test_point.txt";
        String owner = "test_owner";
        String expectedPath = userStoragePath +   File.separator + "backups" + File.separator + database +  File.separator + point;
        System.out.println(expectedPath);
        File file = new File(expectedPath);
        file.getParentFile().mkdirs();
        file.createNewFile();

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class);
                 MockedStatic<UserService> mockedUserService = mockStatic(UserService.class)) {
                mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
                mockedUserService.when(() -> UserService.userStoragePath(anyString())).thenReturn(userStoragePath);

                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                BackupService.delete(owner, database, point);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(DELETE_BACKUP), eq(false));
                verify(query).addParameter(eq("database"), eq(database));
                verify(query).addParameter(eq("point"), eq(point));
                verify(query).executeUpdate();
            }
        }
    }

    @Test
    @DisplayName("Shouldn't delete Backup and throw Runtime Exception")
    public void testShouldNotDeleteBackup() {
        String userStoragePath = "test_user_path_not_existing";
        String database = "test_db";
        String point = "test_point.txt";
        String owner = "test_owner";

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class);
                 MockedStatic<UserService> mockedUserService = mockStatic(UserService.class)) {
                mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
                mockedUserService.when(() -> UserService.userStoragePath(anyString())).thenReturn(userStoragePath);

                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                RuntimeException re = assertThrows(RuntimeException.class, () -> BackupService.delete(owner, database, point));
                assertEquals("File not deleted", re.getMessage());
            }
        }
    }






    }


