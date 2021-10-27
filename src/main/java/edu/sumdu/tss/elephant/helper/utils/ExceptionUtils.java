package edu.sumdu.tss.elephant.helper.utils;

import edu.sumdu.tss.elephant.helper.Keys;
import io.javalin.core.util.JavalinLogger;
import io.javalin.core.validation.ValidationException;
import io.javalin.http.Context;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    private static final CharSequence SQL_UNIQ_EXCEPTION_MESSAGE = "duplicate key value violates unique constraint";

    public static String validationMessages(io.javalin.core.validation.ValidationException ex) {
        StringBuilder message = new StringBuilder();
        var errors = ex.getErrors();
        message.append("<ul>");
        for (var entry : errors.entrySet()) {
            message.append("<li><b>").append(entry.getKey()).append("</b>&nbsp;");
            for (var cause : entry.getValue()) {
                message.append(cause.getMessage()).append(" ");
            }
            message.append("</li>");
        }
        message.append("</ul>");
        return message.toString();
    }

    public static boolean isSQLUniqueException(Throwable ex) {
        if (ex.getMessage() != null && ex.getMessage().contains(SQL_UNIQ_EXCEPTION_MESSAGE)) {
            return true;
        }
        var cause = ex.getCause();
        return (cause != null) && isSQLUniqueException(cause);
    }

    public static String stacktrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static void wrapError(Context context, Exception ex) {
        String cause;
        if (ex instanceof ValidationException) {
            cause = ExceptionUtils.validationMessages((io.javalin.core.validation.ValidationException) ex);
        } else {
            cause = ex.getMessage();
            JavalinLogger.error("Exception:", ex);
        }
        context.sessionAttribute(Keys.ERROR_KEY, cause);
    }
}
