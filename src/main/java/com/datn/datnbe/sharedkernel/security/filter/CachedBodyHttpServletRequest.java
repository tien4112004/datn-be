package com.datn.datnbe.sharedkernel.security.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * HttpServletRequest wrapper that caches the request body for multiple reads.
 * This is necessary for logging the request body since ServletInputStream can only be read once.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedBody;
    private String cachedBodyString;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // Read and cache the body on initialization
        cacheBody();
    }

    private void cacheBody() throws IOException {
        ServletInputStream inputStream = super.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        this.cachedBody = buffer.toByteArray();
        this.cachedBodyString = new String(cachedBody, StandardCharsets.UTF_8);
    }

    /**
     * Get the cached body as a String
     * @return the request body as string
     */
    public String getBody() {
        return cachedBodyString;
    }

    /**
     * Override getInputStream to return a new ByteArrayInputStream with the cached body.
     * This allows the body to be read multiple times.
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(new ByteArrayInputStream(cachedBody));
    }

    /**
     * Override getReader to read from the cached body.
     * This allows the body to be read multiple times.
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), StandardCharsets.UTF_8));
    }

    /**
     * Inner class to wrap ByteArrayInputStream as ServletInputStream.
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream byteArrayInputStream;

        public CachedBodyServletInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.byteArrayInputStream = byteArrayInputStream;
        }

        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return byteArrayInputStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return byteArrayInputStream.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            // Not implemented for cached streams
        }
    }
}
