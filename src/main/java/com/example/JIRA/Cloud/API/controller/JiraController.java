package com.example.JIRA.Cloud.API.controller;

import com.example.JIRA.Cloud.API.dto.JiraIssue;
import com.example.JIRA.Cloud.API.entity.JiraIssueStaging;
import com.example.JIRA.Cloud.API.service.JiraService;
import com.example.JIRA.Cloud.API.service.JiraStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/jira")
@RequiredArgsConstructor
public class JiraController {
    
    private final JiraService jiraService;
    private final JiraStorageService jiraStorageService;
    
    @GetMapping("/issues/yesterday/{issueType}")
    public ResponseEntity<List<JiraIssue>> getYesterdayIssues(@PathVariable String issueType) {
        List<JiraIssue> issues = jiraService.fetchAllIssuesUpdatedYesterday(issueType);
        return ResponseEntity.ok(issues);
    }
    
    @PostMapping("/issues/sync/{issueType}")
    public ResponseEntity<String> syncIssuesByType(@PathVariable String issueType) {
        List<JiraIssue> issues = jiraService.fetchAllIssuesUpdatedYesterday(issueType);
        jiraStorageService.saveIssues(issues, issueType);
        return ResponseEntity.ok(String.format("Synced %d %s issues to database", issues.size(), issueType));
    }
    
    @GetMapping("/issues/stored/{issueType}")
    public ResponseEntity<List<JiraIssueStaging>> getStoredIssues(
            @PathVariable String issueType,
            @RequestParam(required = false, defaultValue = "24") int hours) {
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(hours);
        
        List<JiraIssueStaging> storedIssues = jiraStorageService.findByIssueType(issueType, startDate, endDate);
        return ResponseEntity.ok(storedIssues);
    }
    
    @GetMapping("/issues/search")
    public ResponseEntity<List<JiraIssue>> searchIssues(
            @RequestParam String jql,
            @RequestParam(required = false, defaultValue = "false") boolean saveToDb) {
        List<JiraIssue> issues = jiraService.fetchAllIssuesWithPagination(jql);
        
        if (saveToDb && !issues.isEmpty()) {
            String issueType = "Custom_Search";
            jiraStorageService.saveIssues(issues, issueType);
        }
        
        return ResponseEntity.ok(issues);
    }
}