package com.datn.datnbe.student.service;

import com.datn.datnbe.student.dto.request.StudentCsvRow;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for parsing CSV files for student import.
 * Uses Apache Commons CSV for robust handling of escaped quotes and complex CSV format variations.
 */
@Service
@Slf4j
public class CsvParserService {

    private static final Set<String> REQUIRED_HEADERS = Set.of("fullName");

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
     * Uses Apache Commons CSV for robust handling of escaped quotes within quoted fields.
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

            Map<String, Integer> headerMap = buildHeaderMap(csvParser.getHeaderMap(), errors);
            if (!errors.isEmpty()) {
                return new ParseResult(rows, errors);
            }

            int rowNumber = 1; // Data starts at row 1 (row 0 is header)
            for (CSVRecord record : csvParser) {
                rowNumber++;
                if (record.size() == 0 || record.get(0).isBlank()) {
                    continue;
                }

                try {
                    StudentCsvRow row = parseDataLine(record, headerMap, rowNumber, errors);
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

    private Map<String, Integer> buildHeaderMap(Map<String, Integer> csvHeaderMap, List<String> errors) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (Map.Entry<String, Integer> entry : csvHeaderMap.entrySet()) {
            String header = entry.getKey().trim();
            if (VALID_HEADERS.contains(header)) {
                headerMap.put(header, entry.getValue());
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

    private StudentCsvRow parseDataLine(CSVRecord record,
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
        if (fullName == null || fullName.isBlank()) {
            rowErrors.add("fullName is required");
        }

        // Parse dateOfBirth if provided
        LocalDate dateOfBirth = null;
        if (dateOfBirthStr != null && !dateOfBirthStr.isBlank()) {
            try {
                dateOfBirth = LocalDate.parse(dateOfBirthStr, DATE_FORMATTER);
            } catch (Exception e) {
                rowErrors.add("Invalid dateOfBirth format (expected YYYY-MM-DD): " + dateOfBirthStr);
            }
        }

        // Parse enrollment date if provided
        LocalDate enrollmentDate = null;
        if (enrollmentDateStr != null && !enrollmentDateStr.isBlank()) {
            try {
                enrollmentDate = LocalDate.parse(enrollmentDateStr, DATE_FORMATTER);
            } catch (Exception e) {
                rowErrors.add("Invalid enrollmentDate format (expected YYYY-MM-DD): " + enrollmentDateStr);
            }
        }

        if (!rowErrors.isEmpty()) {
            errors.add(String.format("Row %d: %s", rowNumber, String.join(", ", rowErrors)));
            return null;
        }

        return StudentCsvRow.builder()
                .fullName(fullName)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .address(address)
                .parentName(parentName)
                .parentPhone(parentPhone)
                .parentContactEmail(parentContactEmail)
                .classId(classId)
                .enrollmentDate(enrollmentDate)
                .status(status)
                .build();
    }

    private String getValueFromRecord(CSVRecord record, String columnName) {
        try {
            if (!record.isMapped(columnName)) {
                return null;
            }
            String value = record.get(columnName).trim();
            return value.isEmpty() ? null : value;
        } catch (IllegalArgumentException e) {
            return null;
        }
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