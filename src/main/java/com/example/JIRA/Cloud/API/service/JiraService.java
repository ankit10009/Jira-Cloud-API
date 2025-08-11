package com.example.JIRA.Cloud.API.service;

import com.example.JIRA.Cloud.API.config.JiraConfig;
import com.example.JIRA.Cloud.API.dto.JiraIssue;
import com.example.JIRA.Cloud.API.dto.JiraSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraService {
    
    private final RestClient restClient;
    private final JiraConfig jiraConfig;
    
    public List<JiraIssue> fetchAllIssuesUpdatedYesterday(String issueType) {
        String jql = buildJqlForYesterday(issueType);
        return fetchAllIssuesWithPagination(jql);
    }
    
    public List<JiraIssue> fetchAllIssuesWithPagination(String jql) {
        List<JiraIssue> allIssues = new ArrayList<>();
        int startAt = 0;
        int maxResults = jiraConfig.getMaxResults();
        boolean hasMore = true;
        
        while (hasMore) {
            try {
                log.info("Fetching JIRA issues with JQL: {} (startAt: {}, maxResults: {})", 
                        jql, startAt, maxResults);
                
                JiraSearchResponse response = searchIssues(jql, startAt, maxResults);
                
                if (response != null && response.getIssues() != null) {
                    allIssues.addAll(response.getIssues());
                    log.info("Fetched {} issues in this batch. Total so far: {}", 
                            response.getIssues().size(), allIssues.size());
                    
                    // Check if there are more issues to fetch
                    hasMore = response.getIssues().size() == maxResults && 
                             allIssues.size() < response.getTotal();
                    startAt += maxResults;
                } else {
                    hasMore = false;
                }
                
            } catch (Exception e) {
                log.error("Error fetching issues batch starting at {}", startAt, e);
                hasMore = false;
            }
        }
        
        log.info("Completed fetching all issues. Total: {}", allIssues.size());
        return allIssues;
    }
    
    public JiraSearchResponse searchIssues(String jql, int startAt, int maxResults) {
        try {
            String authHeader = createAuthorizationHeader();
            
            JiraSearchResponse response = restClient.get()
                    .uri(jiraConfig.getBaseUrl() + "/rest/api/3/search")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("jql", jql)
                            .queryParam("expand", "names,schema")
                            .queryParam("fields", buildFieldsParameter())
                            .queryParam("startAt", startAt)
                            .queryParam("maxResults", maxResults)
                            .build())
                    .retrieve()
                    .body(JiraSearchResponse.class);
                    
            return response;
            
        } catch (RestClientResponseException e) {
            log.error("Error fetching issues from JIRA: Status={}, Body={}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch issues from JIRA", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching issues from JIRA", e);
            throw new RuntimeException("Failed to fetch issues from JIRA", e);
        }
    }
    
    private String buildJqlForYesterday(String issueType) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        String jql = String.format(
            "updated >= '%s 00:00' AND updated <= '%s 23:59'", 
            yesterdayString, yesterdayString
        );
        
        // Add issue type filter
        if (issueType != null && !issueType.isEmpty()) {
            jql = String.format("issueType = '%s' AND (%s)", issueType, jql);
        }
        
        // Add project filter if configured
        if (jiraConfig.getProjectKey() != null && !jiraConfig.getProjectKey().isEmpty()) {
            jql = String.format("project = '%s' AND (%s)", jiraConfig.getProjectKey(), jql);
        }
        
        return jql;
    }
    
    private String buildFieldsParameter() {
        return "summary,description,status,priority,assignee,reporter,created,updated," +
               "resolutiondate,issuetype,project,labels,components," +
               "customfield_10000,customfield_10001,customfield_10002";
    }
    
    private String createAuthorizationHeader() {
        String credentials = jiraConfig.getUsername() + ":" + jiraConfig.getApiToken();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }
}