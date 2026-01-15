package util;

import java.util.List;

public class StringUtil {

    public static String csvEscape(String value) {
        if(value == null) return "";

        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"","\"\""); // CSV escaping for quotes

        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }

    public static boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isNotNullOrEmpty(List<String> args) {
        for(String arg : args) {
            if(arg == null || arg.isBlank()) return false;
        }

        return true;
    }

}
