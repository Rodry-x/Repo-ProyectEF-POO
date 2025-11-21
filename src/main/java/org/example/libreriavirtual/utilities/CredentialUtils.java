package org.example.libreriavirtual.utilities;

import java.security.SecureRandom;
import java.util.Locale;

public final class CredentialUtils {
    private static final SecureRandom RND = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%&*()-_=+";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    private CredentialUtils() { /* util */ }

    public static String buildEmailFromName(String nombre, String apellidos) {
        String combined = ((nombre == null ? "" : nombre) + " " + (apellidos == null ? "" : apellidos)).trim();
        if (combined.isEmpty()) {
            return "user" + (System.currentTimeMillis() % 10000) + "@libreria.pe";
        }
        String sanitized = combined.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", ".")
                .replaceAll("[^a-z0-9.]", "");
        sanitized = sanitized.replaceAll("^\\.+|\\.+$", "");
        if (sanitized.isEmpty()) {
            return "user" + (System.currentTimeMillis() % 10000) + "@libreria.pe";
        }
        return sanitized + "@libreria.pe";
    }

    public static String generateRandomPassword(int length) {
        int minLength = Math.max(9, length);
        StringBuilder sb = new StringBuilder(minLength);

        // garantizar al menos una mayúscula, una minúscula y un dígito
        sb.append(UPPER.charAt(RND.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RND.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(RND.nextInt(DIGITS.length())));

        for (int i = 3; i < minLength; i++) {
            sb.append(ALL.charAt(RND.nextInt(ALL.length())));
        }

        // barajar
        char[] pwd = sb.toString().toCharArray();
        for (int i = pwd.length - 1; i > 0; i--) {
            int j = RND.nextInt(i + 1);
            char tmp = pwd[i];
            pwd[i] = pwd[j];
            pwd[j] = tmp;
        }
        return new String(pwd);
    }
}
