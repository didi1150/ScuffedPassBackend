package me.didi.PWMBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@EnableScheduling
@SpringBootApplication
public class PwmBackendApplication {
	public static void main(String[] args) {
		System.out.println("Running version with email provider");
		SpringApplication.run(PwmBackendApplication.class, args);
	}
}
