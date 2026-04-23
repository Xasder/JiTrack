package com.xasder.jiratimelog.controller;

import com.xasder.jiratimelog.model.DayResult;
import com.xasder.jiratimelog.model.JiraCredentials;
import com.xasder.jiratimelog.service.JiraTimeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TimeLogController {

    private final JiraTimeService jiraTimeService;

    @GetMapping("/timelog")
    public String timeLogPage(HttpSession session) {
        JiraCredentials creds = (JiraCredentials) session.getAttribute("jriCreds");
        if (creds == null) {
            return "redirect:/";
        }
        return "timelog";
    }

    @PostMapping("/timelog/apply")
    public String apply(@RequestParam String startDate,
                        @RequestParam String endDate,
                        HttpSession session,
                        Model model) {
        JiraCredentials creds = (JiraCredentials) session.getAttribute("jriCreds");
        if (creds == null) {
            log.warn("No credentials in session, redirecting to login");
            return "redirect:/";
        }
        log.info("Fetching time logs from {} to {} for user {}", startDate, endDate, creds.getEmail());
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        try {
            List<DayResult> results = jiraTimeService.fetchTimeLogs(creds, start, end);
            log.info("Found {} results", results.size());
            model.addAttribute("results", results);
            
            // Calculate summary data
            long totalSeconds = 0;
            int totalIssues = 0;
            for (DayResult day : results) {
                totalSeconds += day.getTotalSeconds();
                totalIssues += day.getIssues().size();
            }
            int numDays = (int) (end.toEpochDay() - start.toEpochDay() + 1);
            double totalHours = totalSeconds / 3600.0;
            double expectedHours = numDays * 8.0;
            double expectedSeconds = expectedHours * 3600;
            
            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);
            model.addAttribute("numDays", numDays);
            model.addAttribute("totalIssues", totalIssues);
            model.addAttribute("totalHours", totalHours);
            model.addAttribute("expectedHours", expectedHours);
            model.addAttribute("goTouchGrass", totalSeconds > expectedSeconds * 1.3);
            
            return "results";
        } catch (Exception e) {
            log.error("Error fetching time logs: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            return "timelog";
        }
    }
}
