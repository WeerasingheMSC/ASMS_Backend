package com.example.demo;

import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class AsmsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsmsBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initializeAdmin(AuthService authService) {
		return args -> {
			authService.initializeAdmin();
		};
	}
}
