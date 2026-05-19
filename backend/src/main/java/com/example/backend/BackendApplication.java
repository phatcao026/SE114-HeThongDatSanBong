package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackendApplication {

	private static final String APP_TIME_ZONE = "Asia/Ho_Chi_Minh";

	static {
		TimeZone.setDefault(TimeZone.getTimeZone(APP_TIME_ZONE));
		System.setProperty("user.timezone", APP_TIME_ZONE);
	}

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
