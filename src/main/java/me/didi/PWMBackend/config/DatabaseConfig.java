package me.didi.PWMBackend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import me.didi.PWMBackend.service.RoleService;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
	private final RoleService service;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (service.findByName("user") == null) {
				service.saveRole("user");
			}
			if (service.findByName("admin") == null) {
				service.saveRole("admin");
			}
		};
	}

}
