package edu.sumdu.tss.elephant.middleware;

import edu.sumdu.tss.elephant.helper.exception.CheckTokenException;
import io.javalin.http.Context;

import java.util.Optional;

public class CSRFFilter {

    public static void check(Context ctx) {
        if (!ctx.req.getMethod().equals("POST")) {
            return;
        }
        String currentToken = Optional.ofNullable(ctx.header("X-CSRF-TOKEN")).orElse(ctx.formParam("_csrf"));
        // ! ctx.sessionAttribute("SessionID") MUST be replaced with real user session related secret
        // we need no CSRF protection for non-auth resources, so "not-auth-user" - not necessary
        // "not-auth-user" - just for example in this project
        String sessionID = Optional.ofNullable((String) ctx.sessionAttribute("SessionID")).orElse(CSRFTokenService.NO_AUTH);
        if (currentToken == null || !CSRFTokenService.validateToken(currentToken, sessionID)) {
            throw new CheckTokenException();
        }
    }

    public static void generate(Context ctx) {
        if (!ctx.req.getMethod().equals("GET")) {
            return;
        }
        String sessionID = Optional.ofNullable((String) ctx.sessionAttribute("SessionID")).orElse(CSRFTokenService.NO_AUTH);
        String newToken = CSRFTokenService.generateToken(sessionID);
        ctx.sessionAttribute("csrf", newToken);
    }

}