package com.sk.fcp.utils;

public class StringUtils {
    public static int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static String getDefaultIfEmpty(String text, String defaultValue) {
        return isEmpty(text) ? defaultValue : text;
    }
} 