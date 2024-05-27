package com.fpl.ultimate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FplUltimateApplication {

	public static void main(String[] args) {
		SpringApplication.run(FplUltimateApplication.class, args);
	}

}
