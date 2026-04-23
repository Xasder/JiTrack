package com.xasder.jiratimelog.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents time spent by the user on a specific date.
 */
@Setter
@Getter
public class DateResult {
    private LocalDate date;
    private long seconds;

    public DateResult() {}

    public DateResult(LocalDate date, long seconds) {
        this.date = date;
        this.seconds = seconds;
    }

}
