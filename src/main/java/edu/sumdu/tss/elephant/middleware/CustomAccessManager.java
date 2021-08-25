package edu.sumdu.tss.elephant.middleware;

import edu.sumdu.tss.elephant.controller.HomeController;
import edu.sumdu.tss.elephant.controller.LoginController;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.core.security.AccessManager;
import io.javalin.http.Context;

/**
 * Access manager class
 */
public class CustomAccessManager {
    /**
     * Access manager to block unauthorized api calls
     */
    public static final AccessManager accessManager = (handler, ctx, permittedRoles) -> {
        System.out.println("CAM:[" + ctx.method() + "]" + ctx.path());
        if (permittedRoles.size() == 0) {
            //No roles->no permission check
            System.out.println("CAM: No roles->no permission check");
            handler.handle(ctx);
            return;
        }
        System.out.println("CAM: Check permission");
        UserRole userRole = getUserRole(ctx);
        System.out.println("Role:" + userRole);
        if (permittedRoles.contains(userRole)) {
            System.out.println("Permission granted");
            handler.handle(ctx);
            return;
        }
        System.out.println("Permission deny");
        ctx.redirect(userRole == UserRole.ANYONE ? LoginController.BASIC_PAGE : HomeController.BASIC_PAGE, 302);
    };

    private static UserRole getUserRole(Context ctx) {
        User currentUser = ctx.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY);
        System.out.println("user from session:" + currentUser);
        if (currentUser == null) {
            return UserRole.ANYONE;
        }

        return currentUser.role();
    }

}
