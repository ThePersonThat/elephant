package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.MailService;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.enums.Lang;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.ResponseUtils;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.core.util.JavalinLogger;
import io.javalin.http.Context;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

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


    //TODO: 404 for email
    private static void resetLink(Context context) {
        if (context.method().equals("POST")) {
            String email = context.formParam("email");
            JavalinLogger.info("email:" + email);
            var user = UserService.byLogin(email);
            var lang = Lang.byValue(user.getLanguage());
            try {
                MailService.sendResetLink(user.getToken(), user.getLogin(), lang);
            } catch (MessagingException e) {
                throw new HttpError500(e);
            }
            context.sessionAttribute(Keys.INFO_KEY, "Check your mail");
            context.redirect(BASIC_PAGE, 302);
            return;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("csrf", context.sessionAttribute("csrf"));
        context.render("/velocity/login/reset-link.vm", model);
    }

    //TODO unknown user
    //TODO validate for empty password
    public static void resetPassword(Context context) {
        if (context.method().equals("POST")) {
            String token = context.formParam("token");
            var password = context.formParam("password");
            var user = UserService.byToken(token);
            user.setPassword(password);
            //TODO: reset token
            UserService.save(user);
            context.sessionAttribute(Keys.INFO_KEY, "Password was reseted");
            context.redirect(BASIC_PAGE);
            return;
        }
        var token = context.queryParam("token");
        Map<String, Object> model = new HashMap<>();
        model.put("csrf", context.sessionAttribute("csrf"));
        model.put("token", token);
        context.render("/velocity/login/reset.vm", model);
    }

    public static void destroy(Context context) {
        ResponseUtils.flush_flash(context);
        context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, null);
        context.redirect(BASIC_PAGE);
    }

    public void register(Javalin app) {
        app.get(BASIC_PAGE, LoginController::show, UserRole.ANYONE);
        app.post(BASIC_PAGE, LoginController::create, UserRole.ANYONE);
        app.get(BASIC_PAGE + "/reset-password", LoginController::resetLink, UserRole.ANYONE);
        app.post(BASIC_PAGE + "/reset-password", LoginController::resetLink, UserRole.ANYONE);
        app.get(BASIC_PAGE + "/reset", LoginController::resetPassword, UserRole.ANYONE);
        app.post(BASIC_PAGE + "/reset", LoginController::resetPassword, UserRole.ANYONE);
        app.get("/logout", LoginController::destroy, UserRole.AUTHED);
    }


}
