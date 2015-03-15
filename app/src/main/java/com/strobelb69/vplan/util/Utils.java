package com.strobelb69.vplan.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Bernd on 15.03.2015.
 */
public class Utils {

    public static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        sw.flush();
        return sw.toString();
    }
}
