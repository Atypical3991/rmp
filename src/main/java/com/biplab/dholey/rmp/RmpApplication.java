package com.biplab.dholey.rmp;

import com.biplab.dholey.rmp.services.KitchenCookService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class RmpApplication {

	public static void main(String[] args) {
		SpringApplication.run(RmpApplication.class, args);
	}

}
