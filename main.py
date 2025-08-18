import os
from datetime import datetime, timedelta
import pandas as pd
from atlassian import Jira
from sqlalchemy import create_engine
from dotenv import load_dotenv
from datetime import datetime, timezone

load_dotenv()

# ---------------- CONFIGURATION ----------------
JIRA_URL = os.getenv("JIRA_URL")
JIRA_EMAIL = os.getenv("JIRA_EMAIL")
JIRA_API_TOKEN = os.getenv("JIRA_API_TOKEN")
DB_URL = os.getenv("DB_URL")

DAYS = int(os.getenv("DAYS", "5"))
PROJECT = os.getenv("PROJECT", None)   # e.g. "ABC"
JQL_EXTRA = os.getenv("JQL_EXTRA", "") # e.g. 'statusCategory != Done'
TABLE_NAME = os.getenv("TABLE_NAME", "jira_issues")

# ------------------------------------------------

# Connect to Jira
jira = Jira(
    url=JIRA_URL,
    username=JIRA_EMAIL,
    password=JIRA_API_TOKEN
)

# Build JQL
since_date = (datetime.now(timezone.utc) - timedelta(days=DAYS)).strftime("%Y-%m-%d %H:%M")
jql = f"updated >= '{since_date}'"
if PROJECT:
    jql = f"project = {PROJECT} AND {jql}"
if JQL_EXTRA:
    jql = f"{jql} AND {JQL_EXTRA}"

print(f"Running JQL: {jql}")

# Fetch issues
issues = jira.jql(jql, limit=1000)

# Flatten issues
data = []
for issue in issues.get("issues", []):
    fields = issue.get("fields", {})

    issue_data = {
        "issue_key": issue.get("key"),
        "issue_id": issue.get("id"),
        "summary": fields.get("summary"),
        "status": fields.get("status", {}).get("name"),
        "assignee": fields.get("assignee", {}).get("displayName") if fields.get("assignee") else None,
        "updated": fields.get("updated"),
    }

    # Handle description (ADF -> plain text if available)
    desc = fields.get("description")
    if isinstance(desc, dict) and "content" in desc:  # ADF format
        def extract_text(adf):
            text_parts = []
            for block in adf.get("content", []):
                if "text" in block:
                    text_parts.append(block["text"])
                if "content" in block:
                    text_parts.append(extract_text(block))
            return " ".join(text_parts)
        issue_data["description"] = extract_text(desc)
    else:
        issue_data["description"] = desc

    # Add all custom fields dynamically
    for k, v in fields.items():
        if k.startswith("customfield_"):
            issue_data[k] = str(v)  # cast to string for DB safety

    data.append(issue_data)

# Convert to DataFrame
df = pd.DataFrame(data)
print(df.head())

# Save to CSV for debug
df.to_csv("jira_issues.csv", index=False)

# ---------------- DB SAVE ----------------
engine = create_engine(DB_URL)

# Append into DB (simple inserts only)
df.to_sql(TABLE_NAME, engine, if_exists="append", index=False)

print("✅ Data inserted successfully into DB (no conflict handling).")
