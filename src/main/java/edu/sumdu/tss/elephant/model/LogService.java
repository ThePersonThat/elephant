package edu.sumdu.tss.elephant.model;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.Keys;
import io.javalin.http.Context;
import org.sql2o.Connection;

public class LogService {

    private static final String INSERT_LOG_SQL = "INSERT INTO LOGGER(ip, database, \"user\", message) values(:ip, :database, :user, :message);";

    public static void push(Context context, String database, String message) {
        try (Connection con = DBPool.getConnection().open()) {
            User user = context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY);
            con.createQuery(INSERT_LOG_SQL)
                    .addParameter("database", database)
                    .addParameter("message", message)
                    .addParameter("user", (user != null) ? user.getLogin() : "Undefined")
                    .addParameter("ip", context.ip())
                    .executeUpdate();
        }
    }
}
