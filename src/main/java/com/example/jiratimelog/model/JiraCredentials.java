package com.example.jiratimelog.model;

/**
 * Simple holder for Jira connection credentials provided by the user).
 */
public class JiraCredentials {
    private String baseUrl;
    private String email;
    private String token;

    public JiraCredentials() {}

    public JiraCredentials(String baseUrl, String email, String token) {
        this.baseUrl = baseUrl;
        this.email = email;
        this.token = token;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
