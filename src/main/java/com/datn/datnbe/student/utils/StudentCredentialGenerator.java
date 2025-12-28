package com.datn.datnbe.student.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.text.Normalizer;

public final class StudentCredentialGenerator {

    private StudentCredentialGenerator() {
        // Utility class, should not be instantiated
    }

    public static String generateUsername(String fullName, LocalDate dateOfBirth) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }

        String normalized = Normalizer.normalize(fullName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String base = normalized.toLowerCase().trim().replaceAll("\\s+", "").replaceAll("[^a-z0-9]", "");

        String dobPart = "nodob";
        if (dateOfBirth != null) {
            dobPart = dateOfBirth.format(DateTimeFormatter.ofPattern("ddMMyy"));
        }

        int suffix = (int) (System.currentTimeMillis() % 1000);
        String suffixStr = String.format("%03d", suffix);

        // Concatenate without separators: {base}{ddMMyy|nodob}{sss}
        return String.format("%s%s%s", base, dobPart, suffixStr);
    }

    public static String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder password = new StringBuilder("Temp@");
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
