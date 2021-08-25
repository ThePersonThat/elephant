package edu.sumdu.tss.elephant;

import edu.sumdu.tss.elephant.controller.*;
import edu.sumdu.tss.elephant.helper.Keys;
import edu.sumdu.tss.elephant.helper.ViewHelper;
import edu.sumdu.tss.elephant.helper.exception.HttpException;
import edu.sumdu.tss.elephant.middleware.CSRFFilter;
import edu.sumdu.tss.elephant.middleware.CustomAccessManager;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.File;

public class Server {
    private static final boolean enableCors = true;
    private final Javalin app;

    {
        app = Javalin.create(
                config -> {
                    config.addStaticFiles("/public");
                    config.accessManager(CustomAccessManager.accessManager);
                })
                .before("/", CSRFFilter::check)
                .before("/", CSRFFilter::generate)
                .before(context -> {
                    context.sessionAttribute(Keys.BREADCRUMB_KEY, null);
                })
                .exception(HttpException.class, ViewHelper::userError)
                .exception(Exception.class, (e, ctx) -> {
                    ViewHelper.userError(new HttpException(e), ctx);
                });
        new ApiController(app);
        new BackupController(app);
        new DatabaseController(app);
        new TableController(app);
        new HomeController(app);
        new LoginController(app);
        new ProfileController(app);
        new RegistrationController(app);
        new ScriptsController(app);
        new SqlController(app);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("File with properties must be specified on startup");
        }
        System.out.println(args);
        var file = new File(args[0]);
        Keys.loadParams(file);

        new Server().start(Integer.valueOf(Keys.get("APP.PORT")));
    }

    /**
     * Method sets the access to sending the requests to the server
     *
     * @param ctx Context that contains the header with access
     */
    public static void cors(Context ctx) {
        ctx.header("Cache-Control", "no-cache, no-store");
        if (enableCors) {
            ctx.header("Access-Control-Allow-Origin", "*");
        }
    }

    public void start(int port) {
        this.app.start(port);
    }

   /*
    public static void main(String[] args) throws IOException, ParseException, SQLException, ClassNotFoundException {
        String config = new String(Files.readAllBytes(Paths.get("config.txt")));
        JSONParser parser = new JSONParser();
        JSONObject jsonConfig = (JSONObject) parser.parse(config);

        Javalin app = Javalin.create().start(8080);

                    .before("/", CSRFFilter::check)
                .before("/", CSRFFilter::generate)
                .routes(() -> {
                    get("/", ServiceController::show);
                    post("/", ServiceController::createOrUpdate);
                    get("*", ServiceController::act);
                    post("*", ServiceController::act);
                })
                .exception(CheckTokenException.class, ViewHelper::noAuth);
        DBController db = new DBController((String) jsonConfig.get("user"), (String) jsonConfig.get("pass"), (String) jsonConfig.get("url"));
        System.out.println("Database connect: [OK]");

        Mail mail = new Mail((String) jsonConfig.get("mail"), (String) jsonConfig.get("mail-pass"), (String) jsonConfig.get("from"));
        System.out.println("SMTP connect: [OK]");

        ActivePing ap = new ActivePing();
        Timer timer = new Timer();

    }
*/

    public void stop() {
        this.app.stop();
    }
}