package com.example.petadoptionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PetAdoptionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetAdoptionServiceApplication.class, args);
	}

}
