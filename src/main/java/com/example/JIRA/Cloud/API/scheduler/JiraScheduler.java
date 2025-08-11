package com.example.JIRA.Cloud.API.scheduler;

import com.example.JIRA.Cloud.API.config.JiraConfig;
import com.example.JIRA.Cloud.API.dto.JiraIssue;
import com.example.JIRA.Cloud.API.service.JiraService;
import com.example.JIRA.Cloud.API.service.JiraStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JiraScheduler {
    
    private final JiraService jiraService;
    private final JiraStorageService jiraStorageService;
    private final JiraConfig jiraConfig;
    
    @Scheduled(cron = "0 0 1 * * ?")
    public void fetchDailyUpdatedIssues() {
        log.info("Starting scheduled JIRA issue fetch job for all configured issue types");
        
        List<String> issueTypes = jiraConfig.getIssueTypes();
        if (issueTypes == null || issueTypes.isEmpty()) {
            log.warn("No issue types configured. Please configure jira.issue-types in application.properties");
            return;
        }
        
        for (String issueType : issueTypes) {
            try {
                log.info("Fetching issues for type: {}", issueType);
                
                List<JiraIssue> issues = jiraService.fetchAllIssuesUpdatedYesterday(issueType);
                
                if (issues != null && !issues.isEmpty()) {
                    log.info("Fetched {} {} issues updated yesterday", issues.size(), issueType);
                    
                    // Save to database
                    jiraStorageService.saveIssues(issues, issueType);
                    
                } else {
                    log.info("No {} issues found for yesterday", issueType);
                }
                
            } catch (Exception e) {
                log.error("Error fetching {} issues", issueType, e);
            }
        }
        
        log.info("Completed scheduled JIRA issue fetch job");
    }
    
    @Scheduled(cron = "0 30 1 * * ?")
    public void fetchStoriesUpdatedYesterday() {
        fetchIssuesByType("Story");
    }
    
    @Scheduled(cron = "0 45 1 * * ?") 
    public void fetchEpicsUpdatedYesterday() {
        fetchIssuesByType("Epic");
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void fetchBugsUpdatedYesterday() {
        fetchIssuesByType("Bug");
    }
    
    @Scheduled(cron = "0 15 2 * * ?")
    public void fetchTasksUpdatedYesterday() {
        fetchIssuesByType("Task");
    }
    
    private void fetchIssuesByType(String issueType) {
        try {
            log.info("Starting scheduled fetch for {} issues updated yesterday", issueType);
            
            List<JiraIssue> issues = jiraService.fetchAllIssuesUpdatedYesterday(issueType);
            
            if (issues != null && !issues.isEmpty()) {
                log.info("Fetched {} {} issues updated yesterday", issues.size(), issueType);
                jiraStorageService.saveIssues(issues, issueType);
            } else {
                log.info("No {} issues found for yesterday", issueType);
            }
            
        } catch (Exception e) {
            log.error("Error during scheduled {} fetch", issueType, e);
        }
    }
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldRecords() {
        try {
            log.info("Starting cleanup of old JIRA records");
            jiraStorageService.cleanupOldRecords(30); // Keep 30 days of data
        } catch (Exception e) {
            log.error("Error during cleanup", e);
        }
    }
    
    @Scheduled(fixedRate = 300000)
    public void healthCheck() {
        log.debug("JIRA Scheduler is running - Health check");
    }
}