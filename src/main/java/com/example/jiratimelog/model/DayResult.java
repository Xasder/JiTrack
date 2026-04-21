package com.example.jiratimelog.model;

import java.time.LocalDate;
import java.util.List;

public class DayResult {
    private LocalDate date;
    private long totalSeconds;
    private List<IssueTimeEntry> issues;

    public DayResult() {}

    public DayResult(LocalDate date, long totalSeconds, List<IssueTimeEntry> issues) {
        this.date = date;
        this.totalSeconds = totalSeconds;
        this.issues = issues;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getTotalSeconds() { return totalSeconds; }
    public void setTotalSeconds(long totalSeconds) { this.totalSeconds = totalSeconds; }
    public List<IssueTimeEntry> getIssues() { return issues; }
    public void setIssues(List<IssueTimeEntry> issues) { this.issues = issues; }
    
    public double getTotalHours() {
        return totalSeconds / 3600.0;
    }
}