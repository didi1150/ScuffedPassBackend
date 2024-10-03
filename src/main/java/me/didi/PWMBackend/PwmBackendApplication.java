package me.didi.PWMBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PwmBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(PwmBackendApplication.class, args);
	}
}
