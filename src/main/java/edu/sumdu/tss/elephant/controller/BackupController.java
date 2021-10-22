package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import edu.sumdu.tss.elephant.model.Backup;
import edu.sumdu.tss.elephant.model.BackupService;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.DatabaseService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class BackupController extends AbstractController {

    public static final String BASIC_PAGE = "/database/{database}/point/";
    private static final ParameterizedStringFactory DEFAULT_CRUMB = new ParameterizedStringFactory("<a href='/database/:database/point'>Backups</a>");

    public BackupController(Javalin app) {
        super(app);
    }

    public static void restore(Context context) {
        Backup point = setupPoint(context);
        BackupService.restore(currentUser(context).getUsername(), point.getDatabase(), point.getPoint());
        context.redirect(BASIC_PAGE.replace("{database}", point.getDatabase()));
    }

    public static void create(Context context) {
        String dbName = context.pathParam("database");
        String point = context.formParam("point");
        if (point == null) {
            point = context.pathParam("point");
        }
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        BackupService.perform(currentUser(context).getUsername(), database.getName(), point);
        context.redirect(BASIC_PAGE.replace("{database}", dbName));
    }

    public static void delete(Context context) {
        Backup point = setupPoint(context);
        BackupService.delete(currentUser(context).getUsername(), point.getDatabase(), point.getPoint());
        context.redirect(BASIC_PAGE.replace("{database}", point.getDatabase()));
    }

    public static void index(Context context) {
        String dbName = context.pathParam("database");
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        var model = ViewHelper.defaultVariables(context);
        model.put("points", BackupService.list(database.getName()));
        ViewHelper.breadcrumb(context).add(DEFAULT_CRUMB.addParameter("database", dbName).toString());
        context.render("/velocity/point/index.vm", model);
    }

    private static Backup setupPoint(Context context) {
        String dbName = context.pathParam("database");
        String point = context.formParam("point");
        if (point == null) {
            point = context.pathParam("point");
        }
        Database database = DatabaseService.activeDatabase(currentUser(context).getUsername(), dbName);
        return BackupService.byName(database.getName(), point);
    }

    public void register(Javalin app) {
        app.get(BASIC_PAGE, BackupController::index, UserRole.AUTHED);
        app.post(BASIC_PAGE, BackupController::create, UserRole.AUTHED);
        app.post(BASIC_PAGE + "{point}/create", BackupController::create, UserRole.AUTHED);
        app.post(BASIC_PAGE + "{point}/reset", BackupController::restore, UserRole.AUTHED);
        app.post(BASIC_PAGE + "{point}/delete", BackupController::delete, UserRole.AUTHED);
    }
}
