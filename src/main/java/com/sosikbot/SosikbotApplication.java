package com.sosikbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SosikbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SosikbotApplication.class, args);
	}

}
