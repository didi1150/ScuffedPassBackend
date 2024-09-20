package me.didi.PWMBackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import me.didi.PWMBackend.service.RoleService;

@Configuration
public class DatabaseConfig {
	@Autowired
	private RoleService service;

	@Bean
	CommandLineRunner initDatabase() {
		return args -> {
			if (service.findByName("user") == null) {
				service.saveRole("user");
			}
		};
	}

}
