package com.xasder.jiratimelog.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple holder for Jira connection credentials provided by the user).
 */
@Setter
@Getter
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

}
