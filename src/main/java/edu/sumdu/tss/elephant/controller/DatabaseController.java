package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
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
        context.render("/velocity/database/show.vm", currentModel(context));
    }

    public static void create(Context context) {
        User user = currentUser(context);
        String dbName = StringUtils.randomAlphaString(8);
        DatabaseService.create(dbName, user.getUsername(), user.getUsername());
        LogService.push(context, dbName, "Database created");
        context.sessionAttribute(Keys.INFO_KEY, "Database created");
        context.redirect(BASIC_PAGE + dbName);
    }

    public static void delete(Context context) {
        Database database = currentDB(context);
        DatabaseService.drop(database);
        LogService.push(context, database.getName(), "Database has been dropped");
        context.sessionAttribute(Keys.INFO_KEY, "Database has been dropped");
        context.redirect(HomeController.BASIC_PAGE);
    }

    @Override
    public void register(Javalin app) {
        app.get(BASIC_PAGE + "{database}", DatabaseController::show, UserRole.AUTHED);
        app.post(BASIC_PAGE + "{database}/delete", DatabaseController::delete, UserRole.AUTHED);
        app.post(BASIC_PAGE, DatabaseController::create, UserRole.AUTHED);
    }


}
