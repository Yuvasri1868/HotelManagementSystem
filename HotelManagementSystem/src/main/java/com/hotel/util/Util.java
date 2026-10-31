package com.hotel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Small helper methods used across the application. */
public class Util {

    /** SHA-256 hash of a password, returned as hex text. */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
