package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.exception.HttpError500;
import edu.sumdu.tss.elephant.helper.exception.NotImplementedException;
import edu.sumdu.tss.elephant.helper.utils.ParameterizedStringFactory;
import edu.sumdu.tss.elephant.helper.utils.StringUtils;
import edu.sumdu.tss.elephant.model.Script;
import edu.sumdu.tss.elephant.model.ScriptService;
import edu.sumdu.tss.elephant.model.User;
import edu.sumdu.tss.elephant.model.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ScriptsController extends AbstractController {

    public static final String BASIC_PAGE = "/database/:database/script/";
    private static final ParameterizedStringFactory DEFAULT_CRUMB = new ParameterizedStringFactory("<a href='/database/:database/script/'>Scripts</a>");

    public ScriptsController(Javalin app) {
        super(app);
    }

    //FIXME: database owner not checked
    private static void create(Context context) {
        String database = context.pathParam("database");
        User currentUser = currentUser(context);
        var file = context.uploadedFile("file");
        String path = UserService.tablespacePath(currentUser.getUsername()) + "\\uploads\\" + database + "\\" + StringUtils.randomAlphaString(20);
        var destinationFile = new File(path);
        try {
            FileUtils.forceMkdirParent(destinationFile);
            FileUtils.copyInputStreamToFile(file.getContent(), destinationFile);
        } catch (IOException ex) {
            throw new HttpError500(ex);
        }
        Script script = new Script();
        script.setFilename(file.getFilename());
        script.setSize(file.getSize());
        script.setPath(path);
        script.setDatabase(database);
        ScriptService.save(script);

        context.redirect(BASIC_PAGE.replace(":database", database));
    }

    private static void index(Context context) {
        String database = context.pathParam("database");
        var model = ViewHelper.defaultVariables(context);
        model.put("scripts", ScriptService.list(database));
        ViewHelper.breadcrumb(context).add(DEFAULT_CRUMB.addParameter("database", database).toString());
        context.render("/velocity/script/index.vm", model);
    }

    private static void show(Context context) {
        throw new NotImplementedException();
    }

    private static void run(Context context) {
        throw new NotImplementedException();
    }

    void register(Javalin app) {
        app.get(BASIC_PAGE, ScriptsController::index);
        app.post(BASIC_PAGE, ScriptsController::create);
        app.get(BASIC_PAGE + ":point", ScriptsController::show);
        app.post(BASIC_PAGE + ":point", ScriptsController::run);
    }
}
