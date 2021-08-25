package edu.sumdu.tss.elephant.helper.utils;

import edu.sumdu.tss.elephant.helper.ViewHelper;
import io.javalin.http.Context;

import java.util.HashMap;

public class ResponseUtils {

    private static final String OK_ANSWER = "Ok";
    private static final String ERROR_ANSWER = "Error";
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";

    public static Object success(String message) {
        return message(OK_ANSWER, message);
    }

    public static Object error(String message) {
        return message(ERROR_ANSWER, message);
    }

    private static Object message(String state, String message) {
        var answer = new <String, String>HashMap();
        answer.put(STATUS_KEY, state);
        answer.put(MESSAGE_KEY, message);
        return answer;
    }

    public static void flush_flash(Context context) {
        for (var key : ViewHelper.FLASH_KEY) {
            context.sessionAttribute(key, null);
        }
    }
}
