package com.xasder.jiratimelog.model;

public class IssueTimeEntry {
    private String issueKey;
    private String summary;
    private long seconds;
    private String comment;

    public IssueTimeEntry() {}

    public IssueTimeEntry(String issueKey, String summary, long seconds, String comment) {
        this.issueKey = issueKey;
        this.summary = summary;
        this.seconds = seconds;
        this.comment = comment;
    }

    public String getIssueKey() { return issueKey; }
    public void setIssueKey(String issueKey) { this.issueKey = issueKey; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public long getSeconds() { return seconds; }
    public void setSeconds(long seconds) { this.seconds = seconds; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public double getHours() {
        return seconds / 3600.0;
    }
}