package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ProfileController extends AbstractController {

    public static final String BASIC_PAGE = "/profile";

    public ProfileController(Javalin app) {
        super(app);
    }

    public static void show(Context context) {
        context.render("/velocity/profile/show.vm", currentModel(context));
    }

    public static void resetPassword(Context context) {
        User user = currentUser(context);
        //TODO add password validation
        String newPassword = context.formParam("password");
        user.setPassword(newPassword);
        UserService.save(user);
        context.redirect(BASIC_PAGE);
    }

    public static void language(Context context) {
        User user = currentUser(context);
        //TODO add lang validation
        user.setLanguage(context.queryParam("lang"));
        UserService.save(user);
        context.redirect(BASIC_PAGE);
    }

    private static void resetDbPassword(Context context) {
        User user = currentUser(context);
        //TODO add password validation
        user.setDbPassword(context.formParam("db-password"));
        UserService.save(user);
        DbUserService.dbUserPasswordReset(user.getUsername(), user.getDbPassword());
        context.redirect(BASIC_PAGE);
    }

    private static void resetWebPassword(Context context) {
        User user = currentUser(context);
        //TODO add password validation
        user.setPassword(context.formParam("web-password"));
        UserService.save(user);
        context.redirect(BASIC_PAGE);
    }


    private static void upgradeUser(Context context) {
        User user = currentUser(context);
        user.setRole(UserRole.valueOf(context.formParam("role")).getValue());
        UserService.save(user);
        context.redirect(BASIC_PAGE);
    }

    private static void removeSelf(Context context) {
        User user = currentUser(context);
        DbUserService.dropUser(user.getUsername());
        //TODO: delete all user-specific files
        //TODO: logout
        //TODO: remove web-user from DB
        context.redirect("/");
    }

    public void register(Javalin app) {
        app.get(BASIC_PAGE + "/lang", ProfileController::language, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/reset-password", ProfileController::resetPassword, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/reset-db", ProfileController::resetDbPassword, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/reset-api", ProfileController::resetWebPassword, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/upgrade", ProfileController::upgradeUser, UserRole.AUTHED);
        app.post(BASIC_PAGE + "/remove-self", ProfileController::removeSelf, UserRole.AUTHED);
        app.get(BASIC_PAGE, ProfileController::show, UserRole.AUTHED);
    }

}
