package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.exception.NotImplementedException;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ProfileController extends AbstractController {

    public static final String BASIC_PAGE = "/profile";

    public ProfileController(Javalin app) {
        super(app);
    }

    public static void show(Context ctx) {
        throw new NotImplementedException();
    }

    public static void update(Context ctr) {
        throw new NotImplementedException();
    }

    public static void resetKeys(Context ctr) {
        throw new NotImplementedException();
    }

    public static void edit(Context ctr) {
        throw new NotImplementedException();
    }

    void register(Javalin app) {
        app.post(BASIC_PAGE, ProfileController::update, UserRole.AUTHED);
        app.get(BASIC_PAGE + "/edit", ProfileController::edit, UserRole.AUTHED);
        app.get(BASIC_PAGE + "/resetKeys", ProfileController::resetKeys, UserRole.AUTHED);
        app.get(BASIC_PAGE, ProfileController::show, UserRole.AUTHED);

    }

}
