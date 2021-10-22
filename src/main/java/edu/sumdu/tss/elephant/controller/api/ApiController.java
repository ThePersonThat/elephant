package edu.sumdu.tss.elephant.controller.api;

import edu.sumdu.tss.elephant.controller.AbstractController;
import edu.sumdu.tss.elephant.helper.Hmac;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.BackupException;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.ResponseUtils;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.core.util.JavalinLogger;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

//FIXME: no time nonce
public final class ApiController extends AbstractController {
    public static final String BASIC_PAGE = "/api/v1/";

    public ApiController(final Javalin app) {
        super(app);
    }

    @OpenApi(
            description = "Create/Update restore point (backup) on database",
            operationId = "create_backup",
            summary = "Create/Update restore point (backup) on database",
            deprecated = false,
            tags = {"backup"},

            // Parameters
            pathParams = {
                    @OpenApiParam(name = "database", description = "your database name"),
                    @OpenApiParam(name = "point", description = "your backup name")
            },
            headers = {
                    @OpenApiParam(name = "publickey"),
                    @OpenApiParam(name = "signature")
            },
            // Body
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Backup.class)),

            // Responses
            responses = {
                    // responses with same status and content type will be auto-grouped to the oneOf composed scheme
                    @OpenApiResponse(status = "204") // No content
            }

    )
    public static void backup(final Context ctx) {
        User currentUser;
        try {
            currentUser = verifyRights(ctx);
        } catch (AccessRestrictedException ex) {
            ctx.json(ResponseUtils.error("Can't validate user"));
            return;
        }

        String dbName = ctx.pathParam("name");
        String pointName = ctx.pathParam("point");
        try {
            Database database = DatabaseService.activeDatabase(currentUser.getUsername(), dbName);
            BackupService.perform(currentUser.getUsername(), database.getName(), pointName);
        } catch (AccessRestrictedException | BackupException | NotFoundException ex) {
            ctx.json(ResponseUtils.error("Backup error" + ex.getMessage()));
        }
        ctx.status(204);
    }

    @OpenApi(
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Backup.class)),
            responses = {
                    @OpenApiResponse(status = "204", content = @OpenApiContent(from = Backup.class))
            }
    )
    public static void restore(final Context ctx) {
        User currentUser;
        try {
            currentUser = verifyRights(ctx);
        } catch (AccessRestrictedException ex) {
            ctx.json(ResponseUtils.error("Can't validate user"));
            return;
        }

        String dbName = ctx.pathParam("name");
        String pointName = ctx.pathParam("point");

        try {
            Database database = DatabaseService.activeDatabase(currentUser.getUsername(), dbName);
            BackupService.restore(currentUser.getUsername(), database.getName(), pointName);
        } catch (AccessRestrictedException | BackupException | NotFoundException ex) {
            ctx.json(ResponseUtils.error("Backup error" + ex.getMessage()));
            return;
        }
        ctx.status(204);
    }

    private static User verifyRights(final Context ctx) throws AccessRestrictedException {
        JavalinLogger.info("Verify rights:");
        JavalinLogger.info(ctx.header("publickey"));
        JavalinLogger.info(ctx.header("signature"));
        JavalinLogger.info(ctx.headerMap().toString());
        String publicKey = ctx.header("publickey");
        String hmac = ctx.header("signature");
        User user = UserService.byPublicKey(publicKey);
        String path = ctx.path();
        try {
            String checkedHmac = Hmac.calculate(path, user.getPrivateKey());
            if (hmac.equals(checkedHmac)) {
                throw new AccessRestrictedException("Invalid sign");
            }
        } catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new AccessRestrictedException(ex);
        }
        return user;
    }

    public void register(final Javalin app) {
        app.post(BASIC_PAGE + "database/{name}/create/{point}", ApiController::backup);
        app.post(BASIC_PAGE + "database/{name}/reset/{point}", ApiController::restore);
    }

}
