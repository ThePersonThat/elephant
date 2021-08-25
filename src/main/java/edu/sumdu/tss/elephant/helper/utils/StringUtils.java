package edu.sumdu.tss.elephant.helper.utils;

import java.util.Random;
import java.util.UUID;

public class StringUtils {

    public static String randomAlphaString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

}
