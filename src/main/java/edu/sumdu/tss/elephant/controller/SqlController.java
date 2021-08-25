package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.DBPool;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.*;

public class SqlController extends AbstractController {

    public static final String BASIC_PAGE = "/database/:database/sql";
    private static final ParameterizedStringFactory DEFAULT_CRUMB = new ParameterizedStringFactory("<a href='/database/:database/sql'>SQL Console</a>");

    public SqlController(Javalin app) {
        super(app);
    }

    private static void show(Context context) {
        String dbName = context.pathParam("database");
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        var model = ViewHelper.defaultVariables(context);
        model.put("query", context.sessionAttribute("query"));
        context.sessionAttribute("query", null);
        ViewHelper.breadcrumb(context).add(DEFAULT_CRUMB.addParameter("database", dbName).toString());
        context.render("/velocity/sql/show.vm", model);
    }

    private static void run(Context context) {
        String query = context.formParam("query");
        context.sessionAttribute("query", query);
        String dbName = context.pathParam("database");
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);

        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        StringBuilder builder = new StringBuilder();
        try {
            conn = DBPool.getConnection(database.getName()).open().getJdbcConnection();

            st = conn.createStatement();
            rs = st.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            String[] columnLabels = new String[rsmd.getColumnCount()];
            builder.append("<div class='datagrid'><table><thead><tr>");
            builder.append("<th>&nbsp;#&nbsp;</th>");
            for (int i = 1; i < columnLabels.length + 1; i++) {
                builder.append("<th>" + rsmd.getColumnLabel(i) + "</th>");
                columnLabels[i - 1] = rsmd.getColumnLabel(i);
            }
            builder.append("</tr><tbody>");

            int rn = 1;
            while (rs.next()) {
                String alt = rn % 2 == 0 ? "<tr class='alt'>" : "<tr>";

                builder.append(alt);
                builder.append("<td>" + rn++ + "</td>");
                for (int i = 0; i < columnLabels.length; i++)
                    builder.append("<td>" + rs.getString(columnLabels[i]) + "</td>");
                builder.append("</tr>");
                if (rn > 300) {
                    builder.append("<tr><td colspan=" + columnLabels.length +
                            "><strong style='color: red;'>300+ (extra rows omitted)</strong></td></tr>");
                    break;
                }
            }
            builder.append("</tbody></table></div>");
            context.result(builder.toString());
        } catch (SQLException e) {
            context.result("<strong style='color: red;'>" + e.getMessage() + "</strong>");
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (st != null) try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void register(Javalin app) {
        app.get(BASIC_PAGE, SqlController::show, UserRole.AUTHED);
        //FIXME: anyone can run a script
        app.post(BASIC_PAGE, SqlController::run);
    }


/*
    String query = request.getParameter("query");
if (StringUtils.trimToNull(query) != null) {

        long start = System.currentTimeMillis();
        out.clear();
        try {
            out.print( executeQuery(query) );
        } catch (SQLException e) {
            out.print();
        }
        out.print("<p>Execution (ms): " + (System.currentTimeMillis() - start) + "</p>" );
    }
%>
*/

}
