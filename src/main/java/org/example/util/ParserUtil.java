package org.example.util;

import org.example.exception.IpFormatException;

public class ParserUtil {
    private ParserUtil() {
    }

    public static int toByte(String str) {
        String[] parts = str.split("\\.");
        if (parts.length != 4) {
            throw new IpFormatException("Wrong ip format " +  str);
        }
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(parts[i]);
        }
        return result;
    }
}
