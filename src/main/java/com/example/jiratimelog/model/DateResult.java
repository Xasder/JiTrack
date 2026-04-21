package com.example.jiratimelog.model;

import java.time.LocalDate;

/**
 * Represents time spent by the user on a specific date.
 */
public class DateResult {
    private LocalDate date;
    private long seconds;

    public DateResult() {}

    public DateResult(LocalDate date, long seconds) {
        this.date = date;
        this.seconds = seconds;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }
}
