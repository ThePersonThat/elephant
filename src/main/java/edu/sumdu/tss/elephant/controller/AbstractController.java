package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.utils.MessageBundle;
import edu.sumdu.tss.elephant.model.Database;
import edu.sumdu.tss.elephant.model.User;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

public abstract class AbstractController {

    /**
     * @param app
     */
    public AbstractController(final Javalin app) {
        this.register(app);
    }

    /**
     * @param context
     * @return
     */
    public static User currentUser(final Context context) {
        return context.sessionAttribute(Keys.SESSION_CURRENT_USER_KEY);
    }

    public static Database currentDB(final Context context) {
        return context.sessionAttribute(Keys.DB_KEY);
    }

    //TODO: move this to/from currentModel
    public static MessageBundle currentMessages(final Context context) {
        MessageBundle mb = null;
        var model = currentModel(context);
        if (model != null) {
            mb = (MessageBundle) model.get("msg");
        }
        if (mb == null) {
            mb = new MessageBundle(Keys.get("DEFAULT_LANG"));
        }
        return mb;
    }

    public static Map<String, Object> currentModel(Context context) {
        return context.sessionAttribute(Keys.MODEL_KEY);
    }

    /**
     * @param app
     */
    public abstract void register(Javalin app);

}
