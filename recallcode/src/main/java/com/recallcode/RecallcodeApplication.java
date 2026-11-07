package com.recallcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecallcodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecallcodeApplication.class, args);
	}

}
