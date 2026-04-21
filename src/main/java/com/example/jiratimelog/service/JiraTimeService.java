package com.example.jiratimelog.service;

import com.example.jiratimelog.model.DayResult;
import com.example.jiratimelog.model.IssueTimeEntry;
import com.example.jiratimelog.model.JiraCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class JiraTimeService {
    private static final Logger logger = LoggerFactory.getLogger(JiraTimeService.class);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;

    private RestTemplate createRestTemplate(String authHeader) {
        RestTemplate rt = new RestTemplate();
        rt.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader);
            return execution.execute(request, body);
        });
        return rt;
    }

    private void checkAuthenticationFailed(HttpHeaders headers) {
        String loginReason = headers.getFirst("X-Seraph-Loginreason");
        if (loginReason != null && loginReason.equals("AUTHENTICATED_FAILED")) {
            logger.error("JIRA authentication failed - X-Seraph-Loginreason: AUTHENTICATED_FAILED");
        }
    }

    private String extractComment(Object commentObj) {
        if (commentObj == null) {
            return "";
        }
        if (commentObj instanceof String comment) {
            if (comment.isEmpty()) {
                return "";
            }
            if (comment.startsWith("{type=doc")) {
                return parseAtlassianDocFormat(comment);
            }
            return comment;
        }
        if (commentObj instanceof Map) {
            Map<String, Object> commentMap = (Map<String, Object>) commentObj;
            if (commentMap.containsKey("content") && commentMap.get("content") instanceof List<?> content) {
                if (content.isEmpty()) {
                    return "";
                }
                return extractTextFromContent(content);
            }
            return commentObj.toString();
        }
        return commentObj.toString();
    }

    private String parseAtlassianDocFormat(String docString) {
        if (docString == null || !docString.contains("content=[")) {
            return "";
        }
        try {
            int start = docString.indexOf("content=[");
            int end = docString.lastIndexOf("}]");
            if (start == -1 || end == -1) {
                return "";
            }
            String contentSection = docString.substring(start + 9, end);
            if (contentSection.isEmpty() || contentSection.equals("]")) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            int textStart;
            int textEnd = 0;
            String searchStr = "text=";
            while ((textStart = contentSection.indexOf(searchStr, textEnd)) != -1) {
                textStart += searchStr.length();
                if (textStart < contentSection.length() && contentSection.charAt(textStart) == '\"') {
                    textStart++;
                    textEnd = contentSection.indexOf("\"}", textStart);
                    if (textEnd == -1) {
                        textEnd = contentSection.indexOf(",", textStart);
                        if (textEnd == -1) {
                            textEnd = contentSection.length();
                        }
                    }
                    sb.append(contentSection, textStart, textEnd);
                } else {
                    textEnd = contentSection.indexOf(",", textStart);
                    if (textEnd == -1) {
                        textEnd = contentSection.indexOf("}", textStart);
                    }
                    if (textEnd == -1) {
                        textEnd = contentSection.length();
                    }
                    String value = contentSection.substring(textStart, textEnd).trim();
                    if (!value.startsWith("[{")) {
                        sb.append(value);
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("Failed to parse Atlassian doc format: {}", e.getMessage());
            return "";
        }
    }

    private String extractTextFromContent(List<?> content) {
        StringBuilder sb = new StringBuilder();
        for (Object block : content) {
            if (block instanceof Map) {
                Map<String, Object> blockMap = (Map<String, Object>) block;
                if (blockMap.containsKey("content") && blockMap.get("content") instanceof List<?> blockContent) {
                    for (Object item : blockContent) {
                        if (item instanceof Map) {
                            Map<String, Object> itemMap = (Map<String, Object>) item;
                            if (itemMap.containsKey("text")) {
                                sb.append(itemMap.get("text"));
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public List<DayResult> fetchTimeLogs(JiraCredentials creds, LocalDate start, LocalDate end) throws Exception {
        if (creds == null || creds.getBaseUrl() == null) {
            throw new IllegalArgumentException("Credentials with baseUrl required");
        }

        logger.info("Base URL: {}, Email: {}", creds.getBaseUrl(), creds.getEmail());

        List<DayResult> results = new ArrayList<>();
        Map<LocalDate, DayResult> dayResults = new LinkedHashMap<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            dayResults.put(date, new DayResult(date, 0L, new ArrayList<>()));
        }

        String auth = creds.getEmail() + ":" + creds.getToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;

        RestTemplate restTemplate = createRestTemplate(authHeader);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String jql = "worklogDate = \"" + date.format(ISO_DATE) + "\" AND worklogAuthor = \"" + creds.getEmail() + "\"";
            String baseUrl = creds.getBaseUrl().replaceAll("/+$", "");
            String url = baseUrl + "/rest/api/3/search/jql?jql=" + jql + "&fields=summary&maxResults=1000";

            logger.info("Fetching issues for date: {}", date);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            checkAuthenticationFailed(resp.getHeaders());
            
            if (resp.getBody() == null || !resp.getBody().containsKey("issues")) {
                logger.warn("No issues found for date: {}", date);
                continue;
            }
            
            List<Map<String, Object>> issues = (List<Map<String, Object>>) resp.getBody().get("issues");
            logger.info("Found {} issues for date {}", issues.size(), date);

            DayResult dayResult = dayResults.get(date);

            for (Map<String, Object> issue : issues) {
                String issueKey = (String) issue.get("key");
                String issueSummary = "N/A";
                if (issue.containsKey("fields") && issue.get("fields") instanceof Map) {
                    Map<String, Object> fields = (Map<String, Object>) issue.get("fields");
                    issueSummary = fields.containsKey("summary") ? (String) fields.get("summary") : "N/A";
                }
                
                String wlUrl = baseUrl + "/rest/api/3/issue/" + issueKey + "/worklog";
                ResponseEntity<Map> wlRespEntity = restTemplate.exchange(wlUrl, HttpMethod.GET, entity, Map.class);
                Map<String, Object> wlResp = wlRespEntity.getBody();
                
                checkAuthenticationFailed(wlRespEntity.getHeaders());
                
                if (wlResp == null || !wlResp.containsKey("worklogs")) {
                    continue;
                }
                
                List<Map<String, Object>> worklogs = (List<Map<String, Object>>) wlResp.get("worklogs");

                for (Map<String, Object> wl : worklogs) {
                    Object authorObj = wl.get("author");
                    String authorEmail = null;
                    
                    if (authorObj instanceof Map) {
                        Map<String, Object> authorMap = (Map<String, Object>) authorObj;
                        authorEmail = (String) authorMap.get("emailAddress");
                    }
                    
                    String started = (String) wl.get("started");
                    Object timeSpentObj = wl.get("timeSpentSeconds");
                    Long timeSpent = timeSpentObj instanceof Number ? ((Number) timeSpentObj).longValue() : null;
                    String comment = extractComment(wl.get("comment"));
                    
                    if (authorEmail != null && authorEmail.equalsIgnoreCase(creds.getEmail()) && started != null && timeSpent != null) {
                        LocalDate wlDate = LocalDate.parse(started.substring(0, 10));
                        if (wlDate.equals(date)) {
                            IssueTimeEntry entry = new IssueTimeEntry(issueKey, issueSummary, timeSpent, comment);
                            dayResult.getIssues().add(entry);
                            dayResult.setTotalSeconds(dayResult.getTotalSeconds() + timeSpent);
                            logger.debug("Added {}s to {} - {}", timeSpent, issueKey, comment);
                        }
                    }
                }
            }
        }

        for (Map.Entry<LocalDate, DayResult> e : dayResults.entrySet()) {
            results.add(e.getValue());
        }
        
        logger.info("Final results: {}", results.stream().map(r -> r.getDate() + "=" + r.getTotalSeconds()).toList());
        return results;
    }
}