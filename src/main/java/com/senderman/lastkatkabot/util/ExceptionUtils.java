package com.senderman.lastkatkabot.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

    public static String stackTraceAsString(Throwable e) {
        var result = new StringWriter();
        e.printStackTrace(new PrintWriter(result));
        return result.toString();
    }

}
