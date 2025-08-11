CREATE TABLE IF NOT EXISTS jira_issue_staging (
    id BIGSERIAL PRIMARY KEY,
    issue_id VARCHAR(255) NOT NULL,
    issue_key VARCHAR(255) NOT NULL UNIQUE,
    issue_type VARCHAR(100),
    summary VARCHAR(1000),
    description TEXT,
    status VARCHAR(100),
    status_category VARCHAR(100),
    priority VARCHAR(100),
    assignee_email VARCHAR(255),
    assignee_display_name VARCHAR(255),
    reporter_email VARCHAR(255),
    reporter_display_name VARCHAR(255),
    project_key VARCHAR(100),
    project_name VARCHAR(255),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    resolution_date TIMESTAMP,
    labels VARCHAR(2000),
    components VARCHAR(1000),
    custom_field_1 VARCHAR(1000),
    custom_field_2 VARCHAR(1000),
    custom_field_3 TEXT,
    raw_json TEXT,
    fetch_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jira_issue_staging_issue_key ON jira_issue_staging(issue_key);
CREATE INDEX IF NOT EXISTS idx_jira_issue_staging_issue_type ON jira_issue_staging(issue_type);
CREATE INDEX IF NOT EXISTS idx_jira_issue_staging_fetch_date ON jira_issue_staging(fetch_date);
CREATE INDEX IF NOT EXISTS idx_jira_issue_staging_updated_date ON jira_issue_staging(updated_date);
CREATE INDEX IF NOT EXISTS idx_jira_issue_staging_project_key ON jira_issue_staging(project_key);