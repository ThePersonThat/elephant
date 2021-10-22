package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.Javalin;
import io.javalin.http.Context;

public abstract class AbstractController {

    /**
     * @param app
     */
    public AbstractController(final Javalin app) {
        this.register(app);
    }

    /**
     * @param ctx
     * @return
     */
    public static User currentUser(final Context ctx) {
        return ctx.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY);
    }

    /**
     * @param app
     */
    public abstract void register(Javalin app);

}
