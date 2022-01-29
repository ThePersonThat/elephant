package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DatabaseServiceTest {
    @Mock
    Sql2o sql2o;
    @Mock
    Connection connection;
    @Mock
    Query query;

    private static final String GET_BY_NAME_SQL = "select * from databases where name = :name";
    private static final String GET_BY_OWNER_SQL = "select * from databases where owner = :owner";
    private static final String DB_SIZE = "SELECT pg_database_size(:database)";
    private static final String PG_DB_SQL = "select datname from pg_database where datname=:name";
    private static final String REGISTER_DATABASE_SQL = "insert into databases(name, owner) values(:name, :owner)";
    private static final String UNREGISTER_DATABASE_SQL = "delete from databases where name=:name and owner= :owner";
    private static final ParameterizedStringFactory DROP_DATABASE_SQL = new ParameterizedStringFactory("DROP DATABASE :name WITH (FORCE);");
    private static final ParameterizedStringFactory CREATE_DATABASE_SQL = new ParameterizedStringFactory("CREATE database :name WITH OWNER :owner TABLESPACE :tablespace");

    @Test
    @DisplayName("Should check db existing (db is exist)")
    public void testDatabaseShouldExist() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test DB";

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeScalar(any(Class.class))).thenReturn(dbName);

                assertTrue(DatabaseService.exists(dbName));

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(PG_DB_SQL));
                verify(query).addParameter(eq("name"), eq(dbName));
                verify(query).executeScalar(same(String.class));

            }
        }
    }

    @Test
    @DisplayName("Should check db existing (db is not exist)")
    public void testDatabaseShouldNotExist() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test DB";

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeScalar(any(Class.class))).thenReturn(null);

                assertFalse(DatabaseService.exists(dbName));

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(PG_DB_SQL));
                verify(query).addParameter(eq("name"), eq(dbName));
                verify(query).executeScalar(same(String.class));

            }
        }
    }

    @Test
    @DisplayName("Should find db by name")
    public void testShouldFindDBByName() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test DB";
            Database db = new Database();
            db.setName(dbName);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(db);

               DatabaseService.byName(dbName);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(GET_BY_NAME_SQL));
                verify(query).addParameter(eq("name"), eq(dbName));
                verify(query).executeAndFetchFirst(same(Database.class));

            }
        }
    }

    @Test
    @DisplayName("Should not find db by name and throw exception")
    public void testShouldNotFindDBByName() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test DB";

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(null);

                NotFoundException nfe = assertThrows(NotFoundException.class, () -> DatabaseService.byName(dbName));
                assertEquals(String.format("Database with name %s not found", dbName), nfe.getMessage());

            }
        }
    }

    @Test
    @DisplayName("Should find dbs of user")
    public void testShouldFindUserDBs() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");

            List<Database> dbs = List.of(
                    new Database(),
                    new Database(),
                    new Database()
            );
            String owner = "test_owner";

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetch(any(Class.class))).thenReturn(dbs);

                DatabaseService.forUser(owner);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(GET_BY_OWNER_SQL));
                verify(query).addParameter(eq("owner"), eq(owner));
                verify(query).executeAndFetch(same(Database.class));

            }
        }
    }

    @Test
    @DisplayName("Should return DB size")
    public void testShouldReturnDBSize() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");

            String database = "test_db";
            Integer testSize = 2;

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeScalar(any(Class.class))).thenReturn(testSize);

                DatabaseService.size(database);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(DB_SIZE));
                verify(query).addParameter(eq("database"), eq(database));
                verify(query).executeScalar(same(Integer.class));

            }
        }
    }

    @Test
    @DisplayName("Should create DB")
    public void testShouldCreateDB() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test_db";
            String owner = "test_owner";
            String tablespace = "test_tablespace";
            String paramQuery = CREATE_DATABASE_SQL.addParameter("name", dbName).addParameter("tablespace", tablespace).addParameter("owner", owner).toString();

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                DatabaseService.create(dbName, owner, tablespace);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(paramQuery), eq(false));
                verify(connection).createQuery(eq(REGISTER_DATABASE_SQL));
                verify(query).addParameter(eq("name"), eq(dbName));
                verify(query).addParameter(eq("owner"), eq(owner));
                verify(query, times(2)).executeUpdate();

            }
        }
    }

    @Test
    @DisplayName("Should find Active DB by name and owner")
    public void testShouldActiveDB() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test DB";
            String owner = "owner";
            Database db = new Database();
            db.setName(dbName);
            db.setOwner(owner);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(db);

                DatabaseService.activeDatabase(owner, dbName);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(GET_BY_NAME_SQL));
                verify(query).addParameter(eq("name"), eq(dbName));
                verify(query).executeAndFetchFirst(same(Database.class));
            }
        }
    }

    @Test
    @DisplayName("Should not find Active DB by name and owner")
    public void testShouldNotReturnActiveDB() {

        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test DB";
            String owner = "owner";
            Database db = new Database();
            db.setName(dbName);
            db.setOwner("diff_owner");

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(db);

                AccessRestrictedException are = assertThrows(AccessRestrictedException.class, () ->  DatabaseService.activeDatabase(owner, dbName));
                assertEquals("Database is inaccessible for this user", are.getMessage());
                mockedPool.verify(DBPool::getConnection);

            }
        }
    }

    @Test
    @DisplayName("Should drop DB by name")
    public void testShouldDropDBByName() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "test_db";
            String owner = "test_owner";
            Database database = new Database();
            database.setName(dbName);
            database.setOwner(owner);
            String paramQuery = DROP_DATABASE_SQL.addParameter("name", dbName).toString();

            List<Backup> backups =  List.of(new Backup(),new Backup());
            List<Script> scripts =  List.of(new Script(),new Script());
            for (Backup b : backups) {
                b.setPoint("test");
                b.setDatabase(dbName);
            }
            for (Script s : scripts) {
                s.setDatabase(dbName);
            }

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class);
                 MockedStatic<BackupService> mockedBackupService = mockStatic(BackupService.class);
                 MockedStatic<ScriptService> mockedScriptService = mockStatic(ScriptService.class);
                ) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);
                mockedBackupService.when(() -> BackupService.list(anyString())).thenReturn(backups);mockedScriptService.when(() -> ScriptService.list(anyString())).thenReturn(scripts);

                DatabaseService.drop(database);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(paramQuery), eq(false));
                verify(connection).createQuery(eq(UNREGISTER_DATABASE_SQL));
                verify(query).addParameter(eq("name"), eq(dbName));
                verify(query).addParameter(eq("owner"), eq(owner));
                verify(query, times(2)).executeUpdate();

                mockedBackupService.verify(() -> BackupService.list(eq(dbName)));
                mockedBackupService.verify(() -> BackupService.delete(anyString(),anyString(), anyString()), atMost(backups.size()));
                mockedScriptService.verify(() -> ScriptService.list(eq(dbName)));
                mockedScriptService.verify(() -> ScriptService.destroy(any(Script.class)), atMost(scripts.size()));

            }

        }
    }

}
