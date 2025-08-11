package com.example.JIRA.Cloud.API.service;

import com.example.JIRA.Cloud.API.dto.JiraIssue;
import com.example.JIRA.Cloud.API.entity.JiraIssueStaging;
import com.example.JIRA.Cloud.API.repository.JiraIssueStagingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraStorageService {
    
    private final JiraIssueStagingRepository repository;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    @Transactional
    public void saveIssues(List<JiraIssue> issues, String issueType) {
        log.info("Saving {} issues of type {} to database", issues.size(), issueType);
        
        int savedCount = 0;
        int updatedCount = 0;
        
        for (JiraIssue issue : issues) {
            try {
                JiraIssueStaging stagingEntity = convertToEntity(issue);
                
                Optional<JiraIssueStaging> existingIssue = repository.findByIssueKey(issue.getKey());
                
                if (existingIssue.isPresent()) {
                    JiraIssueStaging existing = existingIssue.get();
                    updateEntityFromDto(existing, stagingEntity);
                    repository.save(existing);
                    updatedCount++;
                } else {
                    repository.save(stagingEntity);
                    savedCount++;
                }
                
            } catch (Exception e) {
                log.error("Error saving issue {}: {}", issue.getKey(), e.getMessage());
            }
        }
        
        log.info("Completed saving issues. New: {}, Updated: {}", savedCount, updatedCount);
    }
    
    private JiraIssueStaging convertToEntity(JiraIssue issue) {
        try {
            JiraIssueStaging.JiraIssueStagingBuilder builder = JiraIssueStaging.builder()
                    .issueId(issue.getId())
                    .issueKey(issue.getKey())
                    .rawJson(objectMapper.writeValueAsString(issue));
            
            if (issue.getFields() != null) {
                JiraIssue.IssueFields fields = issue.getFields();
                
                builder.summary(fields.getSummary())
                       .description(fields.getDescription())
                       .createdDate(parseDateTime(fields.getCreated()))
                       .updatedDate(parseDateTime(fields.getUpdated()))
                       .resolutionDate(parseDateTime(fields.getResolutionDate()))
                       .customField1(fields.getCustomField1())
                       .customField2(fields.getCustomField2())
                       .customField3(convertObjectToString(fields.getCustomField3()));
                
                // Issue Type
                if (fields.getIssueType() != null) {
                    builder.issueType(fields.getIssueType().getName());
                }
                
                // Status
                if (fields.getStatus() != null) {
                    builder.status(fields.getStatus().getName());
                    if (fields.getStatus().getStatusCategory() != null) {
                        builder.statusCategory(fields.getStatus().getStatusCategory().getName());
                    }
                }
                
                // Priority
                if (fields.getPriority() != null) {
                    builder.priority(fields.getPriority().getName());
                }
                
                // Assignee
                if (fields.getAssignee() != null) {
                    builder.assigneeEmail(fields.getAssignee().getEmailAddress())
                           .assigneeDisplayName(fields.getAssignee().getDisplayName());
                }
                
                // Reporter
                if (fields.getReporter() != null) {
                    builder.reporterEmail(fields.getReporter().getEmailAddress())
                           .reporterDisplayName(fields.getReporter().getDisplayName());
                }
                
                // Project
                if (fields.getProject() != null) {
                    builder.projectKey(fields.getProject().getKey())
                           .projectName(fields.getProject().getName());
                }
                
                // Labels
                if (fields.getLabels() != null && fields.getLabels().length > 0) {
                    builder.labels(String.join(",", fields.getLabels()));
                }
                
                // Components
                if (fields.getComponents() != null && fields.getComponents().length > 0) {
                    String components = String.join(",", 
                            java.util.Arrays.stream(fields.getComponents())
                                    .map(JiraIssue.Component::getName)
                                    .collect(Collectors.toList()));
                    builder.components(components);
                }
            }
            
            return builder.build();
            
        } catch (JsonProcessingException e) {
            log.error("Error converting issue to JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to convert issue to JSON", e);
        }
    }
    
    private void updateEntityFromDto(JiraIssueStaging existing, JiraIssueStaging newData) {
        existing.setSummary(newData.getSummary());
        existing.setDescription(newData.getDescription());
        existing.setStatus(newData.getStatus());
        existing.setStatusCategory(newData.getStatusCategory());
        existing.setPriority(newData.getPriority());
        existing.setAssigneeEmail(newData.getAssigneeEmail());
        existing.setAssigneeDisplayName(newData.getAssigneeDisplayName());
        existing.setReporterEmail(newData.getReporterEmail());
        existing.setReporterDisplayName(newData.getReporterDisplayName());
        existing.setUpdatedDate(newData.getUpdatedDate());
        existing.setResolutionDate(newData.getResolutionDate());
        existing.setLabels(newData.getLabels());
        existing.setComponents(newData.getComponents());
        existing.setCustomField1(newData.getCustomField1());
        existing.setCustomField2(newData.getCustomField2());
        existing.setCustomField3(newData.getCustomField3());
        existing.setRawJson(newData.getRawJson());
    }
    
    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeString, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            try {
                // Try alternative format without milliseconds
                DateTimeFormatter alternativeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
                return LocalDateTime.parse(dateTimeString, alternativeFormat);
            } catch (DateTimeParseException e2) {
                log.warn("Failed to parse date: {}", dateTimeString);
                return null;
            }
        }
    }
    
    private String convertObjectToString(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
    
    public List<JiraIssueStaging> findByIssueType(String issueType, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.findByIssueTypeAndFetchDateBetween(issueType, startDate, endDate);
    }
    
    public void cleanupOldRecords(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        repository.deleteOldRecords(cutoffDate);
        log.info("Cleaned up records older than {} days", daysToKeep);
    }
}