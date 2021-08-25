package edu.sumdu.tss.elephant.helper.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    private static final CharSequence SQL_UNIQ_EXCEPTION_MESSAGE = "duplicate key value violates unique constraint";

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
}
