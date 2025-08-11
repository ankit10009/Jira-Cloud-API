package com.example.JIRA.Cloud.API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JiraCloudApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JiraCloudApiApplication.class, args);
	}

}
