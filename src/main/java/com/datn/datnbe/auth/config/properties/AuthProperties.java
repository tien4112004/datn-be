package com.datn.datnbe.auth.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {
    Map<String, String> properties;

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String setProperty(String key, String value) {
        return properties.put(key, value);
    }
}
