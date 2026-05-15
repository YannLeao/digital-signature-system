package com.example.backend;

import com.example.backend.config.EnvFileLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		EnvFileLoader.load();
		SpringApplication.run(BackendApplication.class, args);
	}

}
