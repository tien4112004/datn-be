package com.datn.datnbe.sharedkernel.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Return a 204 No Content for favicon requests to avoid frequent NoResourceFoundException logs
 * (Useful for dev environments where a favicon isn't present)
 */
@Slf4j
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        log.debug("favicon.ico requested, responding with 204 No Content");
        return ResponseEntity.noContent().build();
    }
}
