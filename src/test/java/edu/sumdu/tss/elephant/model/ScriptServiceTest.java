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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScriptServiceTest {
    @Mock
    Sql2o sql2o;
    @Mock
    Connection connection;
    @Mock
    Query query;

    private static final String GET_BY_ID_SQL = "SELECT * from scripts where id = :id;";
    private static final String LIST_SCRIPT_SQL = "SELECT * from scripts where database = :database";
    private static final String DELETE_SQL = "DELETE from scripts where id = :id;";
    private static final String INSERT_SQL =
            "insert into scripts(database, filename, path) " +
                    "values (:database, :filename, :path)";

    @Test
    @DisplayName("Should return script list")
    public void testShouldReturnScriptList() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            String dbName = "testDbName";
            List<Script> scriptList = new ArrayList<Script>();

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), anyString())).thenReturn(query);
                when(query.executeAndFetch(any(Class.class))).thenReturn(scriptList);

                ScriptService.list(dbName);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(LIST_SCRIPT_SQL));
                verify(query).addParameter(eq("database"), eq(dbName));
                verify(query).executeAndFetch(same(Script.class));
            }
        }
    }

    @Test
    @DisplayName("Should save script")
    public void testShouldSaveScript() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            Script script = new Script();

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.bind(script)).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                ScriptService.save(script);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(INSERT_SQL), eq(false));
                verify(query).bind(eq(script));
                verify(query).executeUpdate();

            }
        }
    }

    @Test
    @DisplayName("Should destroy script")
    public void testShouldDeleteScript() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            Script script = new Script();
            script.setPath("testscript.bat");

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString(), anyBoolean())).thenReturn(query);
                when(query.bind(script)).thenReturn(query);
                when(query.executeUpdate()).thenReturn(connection);

                ScriptService.destroy(script);

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(DELETE_SQL), eq(false));
                verify(query).bind(eq(script));
                verify(query).executeUpdate();

            }
        }
    }

    @Test
    @DisplayName("Should return script by id")
    public void testShouldReturnScriptById() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            Script script = new Script();
            script.setId(1);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), any(Integer.class))).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(script);

                ScriptService.byId((int)script.getId());

                mockedPool.verify(DBPool::getConnection);
                verify(connection).createQuery(eq(GET_BY_ID_SQL));
                verify(query).addParameter(eq("id"), eq(Integer.valueOf((int)script.getId())));
                verify(query).executeAndFetchFirst(same(Script.class));

            }
        }
    }

    @Test
    @DisplayName("Should throw exception if the script was not found ")
    public void testShouldThrowExceptionIfScriptNotFound() {
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class)) {
            mockedKeys.when(() -> Keys.get(anyString())).thenReturn("testName");
            Script script = new Script();
            script.setId(1);

            try (MockedStatic<DBPool> mockedPool = mockStatic(DBPool.class)) {
                mockedPool.when(DBPool::getConnection).thenReturn(sql2o);
                when(sql2o.open()).thenReturn(connection);
                when(connection.createQuery(anyString())).thenReturn(query);
                when(query.addParameter(anyString(), any(Integer.class))).thenReturn(query);
                when(query.executeAndFetchFirst(any(Class.class))).thenReturn(null);

                NotFoundException nfe = assertThrows(NotFoundException.class, () -> ScriptService.byId((int)script.getId()));

                assertEquals("File not found",  nfe.getMessage());


            }
        }
    }

}
