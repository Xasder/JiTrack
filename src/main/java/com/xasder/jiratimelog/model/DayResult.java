package com.xasder.jiratimelog.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
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

    public double getTotalHours() {
        return totalSeconds / 3600.0;
    }
}