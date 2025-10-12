package com.datn.datnbe.auth.config.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "app.auth.properties")
public class AuthProperties {
    Map<String, String> properties;

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String setProperty(String key, String value) {
        return properties.put(key, value);
    }
}
