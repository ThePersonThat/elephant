package edu.sumdu.tss.elephant.middleware;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

public class CSRFTokenService {

    // this is wrong! You need no token for no-auth pages
    // Just for example
    public static final String NO_AUTH = "not-auth-user";
    static private String siteWideToken = "test token";

    public static String getSiteWideToken() {
        if (siteWideToken != null) {
            return siteWideToken;
        }
        siteWideToken = System.getenv("CSRF_SECRET");
        if (siteWideToken == null || siteWideToken.isEmpty()) {
            throw new RuntimeException("Three is no environment variable CSRF_SECRET \n See: https://www.jetbrains.com/help/objc/add-environment-variables-and-program-arguments.html#add-environment-variables");
        }
        return siteWideToken;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static String generateToken(String sessionID, long time) {
        String data = sessionID + time;
        HashFunction hash = Hashing.hmacSha256(getSiteWideToken().getBytes(StandardCharsets.UTF_8));
        String token = hash.hashString(data, StandardCharsets.UTF_8).toString();
        return token + '-' + time;
    }

    public static String generateToken(String sessionID) {
        long time = System.currentTimeMillis();
        return generateToken(sessionID, time);
    }

    public static boolean validateToken(String token, String sessionID) {
        int splitter = token.lastIndexOf("-");
        String time = token.substring(splitter + 1);
        String newToken = generateToken(sessionID, Long.parseLong(time));
        return newToken.equals(token);
    }

}
