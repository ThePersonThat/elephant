package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.UserRole;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.model.DatabaseService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class HomeController extends AbstractController {

    public static final String BASIC_PAGE = "/home";

    public HomeController(Javalin app) {
        super(app);
    }

    public static void show(Context context) {
        User current = currentUser(context);
        var model = ViewHelper.defaultVariables(context);
        var bases = DatabaseService.forUser(current.getUsername());
        System.out.println("=========== Home ===========");
        System.out.println(current.getUsername());
        System.out.println("Bases:" + bases.size());
        model.put("bases", bases);
        long usedStorageSize = UserService.storageSize(current.getUsername());
        long maxStorageSize = current.role().maxStorage();
        model.put("sizeUsed", usedStorageSize);
        model.put("sizeTotal", maxStorageSize);
        model.put("spacePercent", Math.round(usedStorageSize * 100.0 / maxStorageSize));
        context.render("/velocity/home/show.vm", model);
    }

    public void register(Javalin app) {
        app.get("/", HomeController::show, UserRole.AUTHED);
        app.get(BASIC_PAGE, HomeController::show, UserRole.AUTHED);
    }


}
