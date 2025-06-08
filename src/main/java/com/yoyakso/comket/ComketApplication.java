package com.yoyakso.comket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ComketApplication {

	public static void main(String[] args) {
		System.out.println("Hello Yoyakso!");
		SpringApplication.run(ComketApplication.class, args);
	}

}
