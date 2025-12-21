package com.datn.datnbe.student.utils;

public final class StudentCredentialGenerator {

    private StudentCredentialGenerator() {
        // Utility class, should not be instantiated
    }

    public static String generateEmail(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }

        String emailPrefix = fullName.toLowerCase()
                .trim()
                .replaceAll("\\s+", ".")
                .replaceAll("[^a-z0-9.]", "");

        return emailPrefix + "@students.local";
    }

    public static String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder password = new StringBuilder("Temp@");
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
