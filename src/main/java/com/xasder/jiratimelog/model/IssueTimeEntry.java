package com.xasder.jiratimelog.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

    public double getHours() {
        return seconds / 3600.0;
    }
}