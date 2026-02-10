package com.datn.datnbe.student.utils;


import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.text.Normalizer;
import com.datn.datnbe.student.repository.StudentRepository;

public final class StudentCredentialGenerator {

    private StudentCredentialGenerator() {
    }

    public static String generateUsername(String fullName, Date dateOfBirth, StudentRepository studentRepository) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or blank");
        }

        String nameBase = extractMiddleAndLastName(fullName);

        String normalized = Normalizer.normalize(nameBase, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String base = normalized.toLowerCase().trim().replaceAll("\\s+", "").replaceAll("[^a-z0-9]", "");

        String dobPart = "nodob";
        if (dateOfBirth != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
            dobPart = sdf.format(dateOfBirth);
        }

        String baseUsername = String.format("%s%s", base, dobPart);

        int count = studentRepository.countExistingUsernames(baseUsername + "%");

        if (count == 0) {
            // Base username doesn't exist, use it
            return baseUsername;
        } else {
            // Add counter: baseUsername_count
            return String.format("%s_%d", baseUsername, count);
        }
    }

    private static String extractMiddleAndLastName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");

        if (parts.length >= 3) {
            StringBuilder result = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1)
                    result.append(" ");
                result.append(parts[i]);
            }
            return result.toString();
        } else {
            return fullName.trim();
        }
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
