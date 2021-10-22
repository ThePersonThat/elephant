package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import edu.sumdu.tss.elephant.model.TableService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Optional;

/**
 * show DB stats
 **/
public class TableController extends AbstractController {

    public static final String BASIC_PAGE = "/database/{database}/table/";
    private static final ParameterizedStringFactory DEFAULT_CRUMB = new ParameterizedStringFactory("<a href='/database/:database/table'>Tables</a>");

    public TableController(Javalin app) {
        super(app);
    }

    public static void index(Context context) {
        System.out.println("Database controller: show");
        String dbName = context.pathParam("database");
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        var tables = TableService.list(database.getName());
        var model = ViewHelper.defaultVariables(context);
        model.put("tables", tables);
        ViewHelper.breadcrumb(context).add(DEFAULT_CRUMB.addParameter("database", dbName).toString());
        context.render("/velocity/table/index.vm", model);
    }

    public static void preview_table(Context context) {
        String dbName = context.pathParam("database");
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        String tableName = context.pathParam("table");
        int limit = Integer.valueOf(Optional.ofNullable(context.queryParam("limit")).orElse("10"));
        int offset = Integer.valueOf(Optional.ofNullable(context.queryParam("offset")).orElse("0"));
        var table = TableService.byName(database.getName(), tableName, limit, offset * limit);
        int size = TableService.getTableSize(database.getName(), tableName);

        var model = ViewHelper.defaultVariables(context);
        model.put("table", table);
        model.put("pager", ViewHelper.pager((size / limit) + 1, offset));
        var breadcrumb = ViewHelper.breadcrumb(context);
        breadcrumb.add(DEFAULT_CRUMB.addParameter("database", dbName).toString());
        breadcrumb.add(tableName);
        context.render("/velocity/table/show.vm", model);
    }

    /* https://www.postgresql.org/docs/current/infoschema-tables.html */

    @Override
    public void register(Javalin app) {
        app.get(BASIC_PAGE, TableController::index, UserRole.AUTHED);
        app.get(BASIC_PAGE + "{table}", TableController::preview_table, UserRole.AUTHED);
    }

}
