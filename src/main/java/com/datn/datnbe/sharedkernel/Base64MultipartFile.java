package com.datn.datnbe.sharedkernel;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Data
public class Base64MultipartFile implements MultipartFile {
    private final byte[] fileContent;
    private final String fileName;
    private final String contentType;
    private final String fieldName;

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(fileContent);
        }
    }
}
