package edu.sumdu.tss.elephant.helper;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.sql2o.Sql2o;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;


class DBPoolTest {

    static MockedStatic<Keys> mockKeys = mockStatic(Keys.class);
    static String defaultDbName = "test";

    @SneakyThrows
    Field getField(String fieldName) {
        // get access to the hashMap
        Field field = DBPool.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        return field;
    }

    @SneakyThrows
    HashMap<String, Pair<Long, Sql2o>> getStorage() {
        return (HashMap<String, Pair<Long, Sql2o>>) getField("storage").get(null);
    }

    @BeforeEach
    @SneakyThrows
    void initialize() {
        mockKeys.when(() -> Keys.get(anyString())).thenReturn(defaultDbName);
    }

    @AfterAll
    static void closeMocks() {
        mockKeys.close();
    }

    @Test
    @SneakyThrows
    @DisplayName("Should create new database connection")
    void testGetConnectionWithNewDatabase() {
        String dbName = "dummyDatabaseName";

        try (MockedConstruction<Sql2o> mockSql = mockConstruction(Sql2o.class)) { // mock creation the Sql2o class
            Sql2o connection = DBPool.getConnection(dbName); // get new connection

            Pair<Long, Sql2o> pair = getStorage().get(dbName); // get value from hash map by key

            assertEquals(connection, pair.value);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Should create default database connection")
    void testGetConnectionWithDefaultDatabase() {
        try (MockedConstruction<Sql2o> mockSql = mockConstruction(Sql2o.class)) { // mock creation the Sql2o class
            Sql2o connection = DBPool.getConnection(); // get new connection with default database

            Pair<Long, Sql2o> pair = getStorage().get(defaultDbName); // get value from hash map by key

            assertEquals(connection, pair.value);
        }
    }

    @Test
    @SneakyThrows
    @DisplayName("Should remove the last used connection of the database")
    void testGetConnectionWhenMoreThenMaxConnection() {
        try (MockedConstruction<Sql2o> mockSql = mockConstruction(Sql2o.class)) { // mock creation the Sql2o class
            HashMap<String, Pair<Long, Sql2o>> storage = getStorage();

            // create 11 connection
            for (int i = 0; i <= 10; i++) {
                DBPool.getConnection(Integer.toString(i));
            }

            String databaseName = "some database name";
            Sql2o connection = DBPool.getConnection(databaseName);

            assertNull(storage.get(Integer.toString(10))); // the 10 connection should be removed

            Pair<Long, Sql2o> pair = storage.get(databaseName); // get the latest connection

            assertEquals(connection, pair.value);
        }
    }

    @Test
    @DisplayName("Should generate correct url")
    void testDbUtilUrl() {
        String dbName = "SomeDatabaseName";
        // the mock Keys class return the defaultDbName value for any key
        String expectedResult = String.format("postgresql://%s:%s@%s:%s/%s", defaultDbName, defaultDbName,
                defaultDbName, defaultDbName, dbName);

        String actualResult = DBPool.dbUtilUrl(dbName);

        assertEquals(expectedResult, actualResult);
    }
}
