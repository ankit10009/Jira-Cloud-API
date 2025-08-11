package com.example.JIRA.Cloud.API.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jira_issue_staging")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiraIssueStaging {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "issue_id", nullable = false)
    private String issueId;
    
    @Column(name = "issue_key", nullable = false, unique = true)
    private String issueKey;
    
    @Column(name = "issue_type")
    private String issueType;
    
    @Column(name = "summary", length = 1000)
    private String summary;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "status_category")
    private String statusCategory;
    
    @Column(name = "priority")
    private String priority;
    
    @Column(name = "assignee_email")
    private String assigneeEmail;
    
    @Column(name = "assignee_display_name")
    private String assigneeDisplayName;
    
    @Column(name = "reporter_email")
    private String reporterEmail;
    
    @Column(name = "reporter_display_name")
    private String reporterDisplayName;
    
    @Column(name = "project_key")
    private String projectKey;
    
    @Column(name = "project_name")
    private String projectName;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;
    
    @Column(name = "labels", length = 2000)
    private String labels;
    
    @Column(name = "components", length = 1000)
    private String components;
    
    @Column(name = "custom_field_1")
    private String customField1;
    
    @Column(name = "custom_field_2")
    private String customField2;
    
    @Column(name = "custom_field_3", columnDefinition = "TEXT")
    private String customField3;
    
    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;
    
    @Column(name = "fetch_date")
    @CreationTimestamp
    private LocalDateTime fetchDate;
    
    @Column(name = "last_modified_date")
    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;
}