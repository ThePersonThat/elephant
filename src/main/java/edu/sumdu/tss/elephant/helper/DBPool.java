package edu.sumdu.tss.elephant.helper;

import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;


public class DBPool {

    public static final String DEFAULT_DATABASE = Keys.get("DB.NAME");
    private static final HashMap<String, Pair<Long, Sql2o>> storage = new HashMap<>();
    private static final int MAX_CONNECTION = 10;
    public static final ParameterizedStringFactory dbUrl =
            new ParameterizedStringFactory(
                    new ParameterizedStringFactory("jdbc:postgresql://:url::port/:database")
                            .addParameter("url", Keys.get("DB.URL"))
                            .addParameter("port", Keys.get("DB.PORT"))
                            .toString());
    private static long counter = 0;

    public static Sql2o getConnection() {
        return getConnection(DEFAULT_DATABASE);
    }

    public static Sql2o getConnection(String dbname) {
        counter += 1;
        boolean is_new = false;
        Pair<Long, Sql2o> temp = storage.get(dbname);
        if (temp == null) {
            if (storage.size() > MAX_CONNECTION) {
                flush();
            }
            temp = new Pair<>();
            is_new = true;
        }

        temp.key = counter;
        if (is_new) {
            temp.value = new Sql2o(dbUrl.addParameter("database", dbname).toString(), Keys.get("DB.USERNAME"), Keys.get("DB.PASSWORD"));
            storage.put(dbname, temp);
        }
        return temp.value;
    }

    public static String dbUtilUrl(String dbName) {
        return String.format("postgresql://%s:%s@%s:%s/%s", Keys.get("DB.USERNAME"), Keys.get("DB.PASSWORD"), Keys.get("DB.URL"), Keys.get("DB.PORT"), dbName);
    }

    private static void flush() {
        Map.Entry<String, Pair<Long, Sql2o>> tmp = null;
        for (Map.Entry<String, Pair<Long, Sql2o>> pair : storage.entrySet()) {
            if (tmp == null) {
                tmp = pair;
            }
            if (tmp.getValue().key < pair.getValue().key) {
                tmp = pair;
            }
        }
        if (tmp != null) {
            storage.remove(tmp.getKey());
        }
    }

}