package com.datn.datnbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class DatnBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatnBeApplication.class, args);
	}

}
