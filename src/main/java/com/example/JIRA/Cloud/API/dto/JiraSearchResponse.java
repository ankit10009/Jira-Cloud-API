package com.example.JIRA.Cloud.API.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraSearchResponse {
    private String expand;
    private int startAt;
    private int maxResults;
    private int total;
    
    @JsonProperty("issues")
    private List<JiraIssue> issues;
}