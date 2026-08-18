package com.example.ex76;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class Ex76Application {

	public static void main(String[] args) {
		SpringApplication.run(Ex76Application.class, args);
		System.out.println("http://localhost:8081/ex76");
	}

}
