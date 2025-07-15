package com.ddf.vodsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VodSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(VodSystemApplication.class, args);
	}

}
