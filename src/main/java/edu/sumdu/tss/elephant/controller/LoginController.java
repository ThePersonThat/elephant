package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.exception.NotImplementedException;
import edu.sumdu.tss.elephant.helper.utils.ResponseUtils;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.core.security.SecurityUtil.roles;

public class LoginController extends AbstractController {

    public static final String BASIC_PAGE = "/login";

    public LoginController(Javalin app) {
        super(app);
    }

    public static void show(Context context) {
        var model = ViewHelper.defaultVariables(context);
        context.render("/velocity/login/show.vm", model);
    }

    public static void create(Context context) {
        String login = context.formParam("login");
        String password = context.formParam("password");
        User user = null;
        try {
            user = UserService.byLogin(login);
        } catch (NotFoundException ex) {
        }
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("well done");
            context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, user);
            context.redirect(HomeController.BASIC_PAGE);
            return;
        }
        context.sessionAttribute(Keys.ERROR_KEY, "User or password not known");
        context.redirect(BASIC_PAGE, 302);
    }

    public static void resetPassword(Context context) {
        Map<String, Object> model = new HashMap<>();
        model.put("csrf", context.sessionAttribute("csrf"));
        context.redirect(HomeController.BASIC_PAGE);
        throw new NotImplementedException();
    }

    public static void destroy(Context context) {
        ResponseUtils.flush_flash(context);
        context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, null);
        context.redirect(BASIC_PAGE);
    }

    void register(Javalin app) {
        app.get(BASIC_PAGE, LoginController::show, roles(UserRole.ANYONE));
        app.post(BASIC_PAGE, LoginController::create, roles(UserRole.ANYONE));
        app.get(BASIC_PAGE + "/reset/:token", LoginController::resetPassword, roles(UserRole.ANYONE));
        app.get("/logout", LoginController::destroy, UserRole.AUTHED);
    }

}
