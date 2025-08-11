# JIRA Cloud API Integration

A Spring Boot application that integrates with JIRA Cloud API to fetch issues updated in the last day, including both system fields and custom fields.

## Features

- Daily scheduled job to fetch issues updated in the previous day
- REST endpoints to manually trigger issue fetching
- Support for system fields and custom fields
- Configurable JIRA connection settings
- Comprehensive logging

## Configuration

Update the following properties in `src/main/resources/application.properties`:

```properties
# JIRA Configuration
jira.base-url=https://your-domain.atlassian.net
jira.username=your-email@domain.com
jira.api-token=your-api-token
jira.project-key=YOUR_PROJECT_KEY
```

### Getting JIRA API Token

1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click "Create API token"
3. Give it a label and copy the generated token
4. Use your email as username and the token as password for Basic Auth

## API Endpoints

- `GET /api/jira/issues/yesterday` - Fetch issues updated yesterday
- `GET /api/jira/issues/search?jql=<JQL_QUERY>` - Search issues with custom JQL

## Scheduled Job

The application runs a scheduled job daily at 1:00 AM to fetch issues updated in the previous day. The schedule can be modified in `JiraScheduler.java`:

```java
@Scheduled(cron = "0 0 1 * * ?") // Daily at 1:00 AM
```

## Custom Fields

The application is configured to fetch the following custom fields:
- `customfield_10000`
- `customfield_10001` 
- `customfield_10002`

To add more custom fields, update the `buildFieldsParameter()` method in `JiraService.java`.

## Running the Application

```bash
mvn spring-boot:run
```

## Dependencies

- Spring Boot 3.5.4
- Spring WebFlux (for WebClient)
- Jackson (for JSON processing)
- Lombok (for boilerplate code reduction)
- PostgreSQL (database dependency)