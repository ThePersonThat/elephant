package edu.sumdu.tss.elephant.controller.api;

import edu.sumdu.tss.elephant.controller.AbstractController;
import edu.sumdu.tss.elephant.helper.Hmac;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.BackupException;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.ResponseUtils;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
        try {
            var database = verifyRights(ctx);
            var pointName = ctx.pathParam("point");
            BackupService.perform(database.getOwner(), database.getName(), pointName);
        } catch (AccessRestrictedException ex) {
            ctx.json(ResponseUtils.error("Can't validate user"));
            return;
        } catch (BackupException | NotFoundException ex) {
            ctx.json(ResponseUtils.error("Backup error" + ex.getMessage()));
            return;
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
        try {
            var database = verifyRights(ctx);
            var pointName = ctx.pathParam("point");
            BackupService.restore(database.getOwner(), database.getName(), pointName);
        } catch (AccessRestrictedException ex) {
            ctx.json(ResponseUtils.error("Can't validate user"));
            return;
        } catch (BackupException | NotFoundException ex) {
            ctx.json(ResponseUtils.error("Backup error" + ex.getMessage()));
            return;
        }
        ctx.status(204);
    }

    private static Database verifyRights(final Context ctx) throws AccessRestrictedException {
        String publicKey = ctx.header("publickey");
        String hmac = ctx.header("signature");
        User user = UserService.byPublicKey(publicKey);
        String path = ctx.path();
        try {
            String checkedHmac = Hmac.calculate(path, user.getPrivateKey());
            if (hmac == null || !hmac.equals(checkedHmac)) {
                throw new AccessRestrictedException("Invalid sign");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new AccessRestrictedException(ex);
        }

        String dbName = ctx.pathParam("name");
        return DatabaseService.activeDatabase(user.getUsername(), dbName);
    }

    public void register(final Javalin app) {
        app.post(BASIC_PAGE + "database/{name}/create/{point}", ApiController::backup);
        app.post(BASIC_PAGE + "database/{name}/reset/{point}", ApiController::restore);
    }

}
