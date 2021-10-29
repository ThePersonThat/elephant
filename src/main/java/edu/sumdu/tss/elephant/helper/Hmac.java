package edu.sumdu.tss.elephant.helper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Hmac {
    private static final String HMAC_SHA384 = "HmacSHA384";

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String calculate(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA384);
        Mac mac = Mac.getInstance(HMAC_SHA384);
        mac.init(secretKeySpec);
        return toHexString(mac.doFinal(data.getBytes()));
    }

}
