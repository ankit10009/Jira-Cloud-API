package com.example.JIRA.Cloud.API.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {
    private String id;
    private String key;
    private String self;
    
    @JsonProperty("fields")
    private IssueFields fields;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueFields {
        private String summary;
        private String description;
        private IssueType issueType;
        private IssueStatus status;
        private IssuePriority priority;
        private User assignee;
        private User reporter;
        private String created;
        private String updated;
        private String resolutionDate;
        
        @JsonProperty("customfield_10000")
        private String customField1;
        
        @JsonProperty("customfield_10001")
        private String customField2;
        
        @JsonProperty("customfield_10002")
        private Object customField3;
        
        @JsonProperty("project")
        private Project project;
        
        @JsonProperty("labels")
        private String[] labels;
        
        @JsonProperty("components")
        private Component[] components;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {
        private String id;
        private String name;
        private String description;
        private boolean subtask;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueStatus {
        private String id;
        private String name;
        private String description;
        private StatusCategory statusCategory;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusCategory {
        private String id;
        private String key;
        private String name;
        private String colorName;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssuePriority {
        private String id;
        private String name;
        private String iconUrl;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String accountId;
        private String emailAddress;
        private String displayName;
        private String accountType;
        private boolean active;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String id;
        private String key;
        private String name;
        private String projectTypeKey;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        private String id;
        private String name;
        private String description;
    }
}