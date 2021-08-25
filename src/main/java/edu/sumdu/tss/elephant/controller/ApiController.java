package edu.sumdu.tss.elephant.controller;

import edu.sumdu.tss.elephant.helper.Hmac;
import edu.sumdu.tss.elephant.helper.exception.AccessRestrictedException;
import edu.sumdu.tss.elephant.helper.exception.BackupException;
import edu.sumdu.tss.elephant.helper.exception.NotFoundException;
import edu.sumdu.tss.elephant.helper.utils.ResponseUtils;
import edu.sumdu.tss.elephant.model.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

//FIXME: no time nonce
public class ApiController extends AbstractController {
    public static final String BASIC_PAGE = "/api/v1/";


    public ApiController(Javalin app) {
        super(app);
    }

    public static void backup(Context ctx) {
        User currentUser;
        try {
            currentUser = verifyRights(ctx);
        } catch (AccessRestrictedException ex) {
            ctx.json(ResponseUtils.error("Can't validate user"));
            return;
        }

        String dbName = ctx.pathParam("name");
        String pointName = ctx.pathParam("point");
        Database database = getDatabase(dbName, currentUser);
        try {
            BackupService.perform(dbName, pointName);
        } catch (BackupException | NotFoundException ex) {
            ctx.json(ResponseUtils.error("Backup error" + ex.getMessage()));
        }
        ctx.json(ResponseUtils.success("Backup performed"));
    }

    public static void restore(Context ctx) {
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
            Database database = getDatabase(dbName, currentUser);
            BackupService.restore(database.getName(), pointName);
        } catch (BackupException | NotFoundException ex) {
            ctx.json(ResponseUtils.error("Backup error" + ex.getMessage()));
            return;
        }
        ctx.json(ResponseUtils.success("Backup performed"));
    }

    private static Database getDatabase(String dbName, User user) throws NotFoundException {
        Database database = DatabaseService.byName(dbName);

        if (!database.getOwner().equals(user.getLogin())) {
            throw new AccessRestrictedException();
        }
        return database;
    }

    private static User verifyRights(Context ctx) throws AccessRestrictedException {
        String publicKey = ctx.header("PUBLIC");
        String hmac = ctx.header("HMAC");
        User user = UserService.getUserByPublicKey(publicKey);
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

    void register(Javalin app) {
        app.post(BASIC_PAGE + "database/:name/create/:point", ApiController::backup);
        app.post(BASIC_PAGE + "database/:name/reset/:point", ApiController::restore);
    }

}
