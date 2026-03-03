package com.example.SmartQueueManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class SmartQueueManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartQueueManagementApplication.class, args);
	}

}
