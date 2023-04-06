package org.example.util;

public class ParserUtil {
    private ParserUtil() {
    }

    public static int toByte(String str) {
        String[] parts = str.split("\\.");
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(parts[i]);
        }
        return result;
    }
}
