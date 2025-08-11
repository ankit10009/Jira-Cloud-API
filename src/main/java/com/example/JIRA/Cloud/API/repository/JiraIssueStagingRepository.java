package com.example.JIRA.Cloud.API.repository;

import com.example.JIRA.Cloud.API.entity.JiraIssueStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JiraIssueStagingRepository extends JpaRepository<JiraIssueStaging, Long> {
    
    Optional<JiraIssueStaging> findByIssueKey(String issueKey);
    
    List<JiraIssueStaging> findByIssueTypeAndFetchDateBetween(
            String issueType, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );
    
    List<JiraIssueStaging> findByFetchDateBetween(
            LocalDateTime startDate, 
            LocalDateTime endDate
    );
    
    @Modifying
    @Transactional
    @Query("DELETE FROM JiraIssueStaging j WHERE j.fetchDate < :cutoffDate")
    void deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    long countByIssueType(String issueType);
    
    @Query("SELECT DISTINCT j.issueType FROM JiraIssueStaging j")
    List<String> findDistinctIssueTypes();
}