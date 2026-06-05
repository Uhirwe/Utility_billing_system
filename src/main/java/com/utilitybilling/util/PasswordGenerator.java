package com.utilitybilling.util;

import java.security.SecureRandom;

public final class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%*?&";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {}

    public static String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();
        password.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        password.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        for (int i = 4; i < 12; i++) {
            password.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }
        return password.toString();
    }
}
