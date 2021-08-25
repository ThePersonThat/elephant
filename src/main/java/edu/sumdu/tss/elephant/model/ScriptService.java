package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import org.sql2o.Connection;

import java.util.List;

public class ScriptService {
    private static final String LIST_SCRIPT_SQL = "SELECT * from scripts where database = :database";
    private static final String GET_BY_ID_SQL = "SELECT * from scripts where id = :id;";
    private static final String INSERT_SQL =
            "insert into scripts(database, filename, path) " +
                    "values (:database, :filename, :path)";

    public static List<Script> list(String dbName) {
        try (Connection con = DBPool.getConnection().open()) {
            return con.createQuery(LIST_SCRIPT_SQL).addParameter("database", dbName).executeAndFetch(Script.class);
        }
    }

    public static Script byId(Integer id) {
        try (Connection con = DBPool.getConnection().open()) {
            Script file = con.createQuery(GET_BY_ID_SQL).addParameter("id", id).executeAndFetchFirst(Script.class);
            if (file == null) {
                throw new NotFoundException("File not found");
            }
            return file;
        }
    }
    //"ON CONFLICT(data) DO UPDATE \n" +
    //"  SET path = excluded.path, \n" +
    //"      \"updatedAt\" = now();";

    public static void save(Script file) {
        try (Connection con = DBPool.getConnection().open()) {
            con.createQuery(INSERT_SQL, false).bind(file).executeUpdate();
        }
    }


}
