package com.nemojin.sosikbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SosikbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(SosikbotApplication.class, args);
	}

}
