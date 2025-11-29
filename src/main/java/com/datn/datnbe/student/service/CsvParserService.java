package com.datn.datnbe.student.service;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service for parsing CSV files for student import.
 */
@Service
@Slf4j
public class CsvParserService {

    private static final Set<String> REQUIRED_HEADERS = Set.of("id", "fullName");

    private static final Set<String> VALID_HEADERS = Set.of("id",
            "fullName",
            "dateOfBirth",
            "gender",
            "address",
            "parentName",
            "parentPhone",
            "parentContactEmail",
            "classId",
            "enrollmentDate",
            "status");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Parse CSV file and return list of StudentCsvRow objects with any parsing errors.
     *
     * @param file the CSV file to parse
     * @return ParseResult containing parsed rows and any errors
     */
    public ParseResult parseStudentCsv(MultipartFile file) {
        List<StudentCsvRow> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("CSV file is empty or not provided");
            return new ParseResult(rows, errors);
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("text/csv") && !contentType.equals("application/vnd.ms-excel")
                && !file.getOriginalFilename().endsWith(".csv")) {
            errors.add("Invalid file type. Please upload a CSV file");
            return new ParseResult(rows, errors);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                        .withAllowMissingColumnNames()
                        .withIgnoreEmptyLines()
                        .parse(reader)) {

            Map<String, Integer> headerMap = parseHeaderLine(headerLine, errors);
            if (!errors.isEmpty()) {
                return new ParseResult(rows, errors);
            }

            String line;
            int rowNumber = 1; // Header is row 0, data starts at row 1
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                try {
                    StudentCsvRow row = parseDataLine(line, headerMap, rowNumber, errors);
                    if (row != null) {
                        rows.add(row);
                    }
                } catch (Exception e) {
                    errors.add(String.format("Row %d: Error parsing row - %s", rowNumber, e.getMessage()));
                }
            }

        } catch (Exception e) {
            log.error("Error reading CSV file", e);
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR, "Error reading CSV file: " + e.getMessage());
        }

        return new ParseResult(rows, errors);
    }

    private Map<String, Integer> parseHeaderLine(String headerLine, List<String> errors) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (Map.Entry<String, Integer> entry : csvHeaderMap.entrySet()) {
            String header = entry.getKey().trim();
            if (VALID_HEADERS.contains(header)) {
                headerMap.put(header, i);
            }
        }

        // Check for required headers
        for (String required : REQUIRED_HEADERS) {
            if (!headerMap.containsKey(required)) {
                errors.add(String.format("Missing required column: %s", required));
            }
        }

        return headerMap;
    }

    private StudentCsvRow parseDataLine(String line,
            Map<String, Integer> headerMap,
            int rowNumber,
            List<String> errors) {

        String fullName = getValueFromRecord(record, "fullName");
        String dateOfBirthStr = getValueFromRecord(record, "dateOfBirth");
        String gender = getValueFromRecord(record, "gender");
        String address = getValueFromRecord(record, "address");
        String parentName = getValueFromRecord(record, "parentName");
        String parentPhone = getValueFromRecord(record, "parentPhone");
        String parentContactEmail = getValueFromRecord(record, "parentContactEmail");
        String classId = getValueFromRecord(record, "classId");
        String enrollmentDateStr = getValueFromRecord(record, "enrollmentDate");
        String status = getValueFromRecord(record, "status");

        // Validate required fields
        List<String> rowErrors = new ArrayList<>();
        if (id == null || id.isBlank()) {
            rowErrors.add("id is required");
        }
        if (fullName == null || fullName.isBlank()) {
            rowErrors.add("fullName is required");
        }

        if (!rowErrors.isEmpty()) {
            errors.add(String.format("Row %d: %s", rowNumber, String.join(", ", rowErrors)));
            return null;
        }

        return StudentCsvRow.builder()
                .id(id)
                .fullName(fullName)
                .dateOfBirth(getValueOrNull(values, headerMap.get("dateOfBirth")))
                .gender(getValueOrNull(values, headerMap.get("gender")))
                .address(getValueOrNull(values, headerMap.get("address")))
                .parentName(getValueOrNull(values, headerMap.get("parentName")))
                .parentPhone(getValueOrNull(values, headerMap.get("parentPhone")))
                .classId(getValueOrNull(values, headerMap.get("classId")))
                .enrollmentDate(getValueOrNull(values, headerMap.get("enrollmentDate")))
                .status(getValueOrNull(values, headerMap.get("status")))
                .createdAt(getValueOrNull(values, headerMap.get("createdAt")))
                .updatedAt(getValueOrNull(values, headerMap.get("updatedAt")))
                .build();
    }

    private String getValueOrNull(String[] values, Integer index) {
        if (index == null || index >= values.length) {
            return null;
        }
        String value = values[index].trim().replace("\"", "");
        return value.isEmpty() ? null : value;
    }

    /**
     * Split CSV line handling quoted values with commas.
     */
    private String[] splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());

        return values.toArray(new String[0]);
    }

    /**
     * Result of parsing operation containing parsed rows and any errors.
     */
    public record ParseResult(List<StudentCsvRow> rows, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}
