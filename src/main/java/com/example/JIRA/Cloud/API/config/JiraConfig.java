package com.example.JIRA.Cloud.API.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "jira")
public class JiraConfig {
    private String baseUrl;
    private String username;
    private String apiToken;
    private String projectKey;
    private List<String> issueTypes;
    private int maxResults = 100;
}