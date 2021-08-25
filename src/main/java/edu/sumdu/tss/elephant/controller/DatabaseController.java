package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import edu.sumdu.tss.elephant.model.LogService;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * show DB stats
 **/
public class DatabaseController extends AbstractController {

    public static final String BASIC_PAGE = "/database/";

    public DatabaseController(Javalin app) {
        super(app);
    }

    public static void show(Context context) {
        String dbName = context.pathParam("database");
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        var model = ViewHelper.defaultVariables(context);
        model.put("databaseSize", DatabaseService.size(database.getName()));
        context.render("/velocity/database/show.vm", model);
    }

    public static void create(Context context) {
        User user = currentUser(context);
        String dbName = StringUtils.randomAlphaString(8);
        DatabaseService.create(dbName, user.getUsername(), user.getUsername());
        LogService.push(context, dbName, "Database created");
        context.redirect(BASIC_PAGE + dbName);
    }

    @Override
    void register(Javalin app) {
        app.get(BASIC_PAGE + ":database", DatabaseController::show, UserRole.AUTHED);
        app.post(BASIC_PAGE, DatabaseController::create, UserRole.AUTHED);
    }


}
