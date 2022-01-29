package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {
    @Mock Sql2o sql2o;
    @Mock Connection connection;
    @Mock Query query;

    final String TABLE_LIST_SQL = """
            SELECT table_name as name
            FROM information_schema.tables
            WHERE table_type='BASE TABLE';""";

    final String TABLE_SIZE_SQL =
            "SELECT n_live_tup FROM pg_stat_user_tables\n" +
                    "WHERE /*schemaname*/ relname = :table";

    String databaseName = "databaseName";

    static MockedStatic<DBPool> mockedPool;

    @BeforeAll
    static void prepareMocks() {
        // prevent static DPool initialization where Keys class with config is required
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            mockedPool = mockStatic(DBPool.class);
        }
    }

    @AfterAll
    static void closeMock() {
        mockedPool.close();
    }

    @BeforeEach
    void methodMocks() {
        mockedPool.clearInvocations();

        // make the getConnection method to return our sql2o
        mockedPool.when(() -> DBPool.getConnection(databaseName)).thenReturn(sql2o);
        when(sql2o.open()).thenReturn(connection); // make the open method to return our connection
        when(connection.createQuery(anyString())).thenReturn(query); // make the createQuery method to return our query
    }

    @Test
    @DisplayName("Should call all method to get the table list")
    void testList() {
        TableService.list(databaseName);

        // all the methods should be called
        mockedPool.verify(() -> DBPool.getConnection(eq(databaseName)));
        verify(sql2o).open();
        verify(connection).createQuery(eq(TABLE_LIST_SQL));
        verify(query).executeAndFetchTable();
    }

    @Test
    @DisplayName("Should call all the methods to call the table size")
    void testGetTableSize() {
        String tableName = "tableName";
        int tableSize = 32;

        // make the addParameter method to return our query
        when(query.addParameter(anyString(), anyString())).thenReturn(query);
        // make the executeScalar method to return our tableSize
        when(query.executeScalar(Integer.class)).thenReturn(tableSize);

        int actualTableSize = TableService.getTableSize(databaseName, tableName);

        // all the methods should be called
        mockedPool.verify(() -> DBPool.getConnection(eq(databaseName)));
        verify(sql2o).open();
        verify(connection).createQuery(eq(TABLE_SIZE_SQL));
        verify(query).addParameter(eq("table"), eq(tableName));
        verify(query).executeScalar(Integer.class);

        // should return the correct table size
        assertEquals(tableSize, actualTableSize);
    }

    @Test
    @DisplayName("Should return 0 if the NPE is thrown")
    void testGetTableSizeWithNPE() {
        int tableSize = TableService.getTableSize(databaseName, "tableName");

        assertEquals(0, tableSize);
    }

    @Test
    @DisplayName("Should return table with offset")
    void testByName() {
        String tableName = "tableName";
        int limit = 10;
        int offset = 5;
        String parametrizedQuery = String.format("select * from %s limit :limit offset :offset", tableName);
        // make the addParameter method to return our query
        when(query.addParameter(anyString(), anyInt())).thenReturn(query);

        TableService.byName(databaseName, tableName, limit, offset);

        // all the methods should be called
        mockedPool.verify(() -> DBPool.getConnection(eq(databaseName)));
        verify(sql2o).open();
        verify(connection).createQuery(eq(parametrizedQuery));
        verify(query).addParameter(eq("limit"), eq(limit));
        verify(query).addParameter(eq("offset"), eq(offset));
        verify(query).executeAndFetchTable();
    }
}