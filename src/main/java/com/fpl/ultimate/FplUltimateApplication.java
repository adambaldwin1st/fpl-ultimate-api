package com.fpl.ultimate;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FplUltimateApplication {

	@Value("${env.path}")
	private String envPath;

	public static void main(String[] args) {
		SpringApplication.run(FplUltimateApplication.class, args);
	}

	@PostConstruct
	public void init() {
		System.out.println("Loading .env file from: " + envPath);
		Dotenv.configure().directory(envPath).filename(".env").load();
	}

}
