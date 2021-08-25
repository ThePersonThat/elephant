package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.MailService;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.enums.Lang;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.ExceptionUtils;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.model.DbUserService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import javax.mail.MessagingException;

import static io.javalin.core.security.SecurityUtil.roles;

public class RegistrationController extends AbstractController {

    public static final String BASIC_PAGE = "/registration";

    public RegistrationController(Javalin app) {
        super(app);
    }

    public static void show(Context ctx) {
        var model = ViewHelper.defaultVariables(ctx);
        ctx.render("/velocity/registration/show.vm", model);
    }

    public static void create(Context context) {
        System.out.println("Start registration");
        try {
            User newUser = UserService.newDefaultUser();
            newUser.setLogin(context.formParam("login"));
            newUser.setPassword(context.formParam("password"));
            UserService.save(newUser);
            context.sessionAttribute("currentUser", newUser);
            DbUserService.initUser(newUser.getUsername(), newUser.getDbPassword());
            System.out.println("Before send mails");
            MailService.sendActivationLink(newUser.getToken(), newUser.getLogin(), Lang.EN);
            System.out.println("After Send mail");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            String cause;
            if (ExceptionUtils.isSQLUniqueException(ex)) {
                cause = "Login (email) already taken";
            } else {
                cause = ex.getMessage();
            }
            context.sessionAttribute(Keys.ERROR_KEY, cause);
            context.redirect(BASIC_PAGE);
            return;
        }
        context.redirect(HomeController.BASIC_PAGE);
    }


    public static void userConformation(Context context) {
        String token = context.pathParam("token");
        User user = UserService.byToken(token);
        if (user == null) {
            throw new NotFoundException("User with this token not found (or token was renewed)");
        }
        user.setRole(UserRole.BASIC_USER.getValue());
        user.setToken(StringUtils.uuid());
        UserService.save(user);
        context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY, user);
        context.sessionAttribute(Keys.INFO_KEY, "Email approved");
        context.redirect(HomeController.BASIC_PAGE);
    }

    public static void resendUserConformation(Context context) {
        var user = currentUser(context);
        try {
            MailService.sendActivationLink(user.getToken(), user.getLogin(), Lang.EN);
            context.sessionAttribute(Keys.INFO_KEY, "Resend conformation email");
        } catch (MessagingException e) {
            throw new HttpError500(e);
        }
        context.redirect(HomeController.BASIC_PAGE);
    }


    public void register(Javalin app) {
        app.get(BASIC_PAGE + "/confirm/:token", RegistrationController::userConformation, roles(UserRole.ANYONE));
        app.get(BASIC_PAGE + "/resend-confirm/", RegistrationController::resendUserConformation, UserRole.AUTHED);
        app.get(BASIC_PAGE, RegistrationController::show, roles(UserRole.ANYONE));
        app.post(BASIC_PAGE, RegistrationController::create, roles(UserRole.ANYONE));
    }


}
