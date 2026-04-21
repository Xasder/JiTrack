package com.xasder.jiratimelog.controller;

import com.xasder.jiratimelog.model.JiraCredentials;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/")
    public String loginForm(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String baseUrl,
                              @RequestParam String email,
                              @RequestParam String token,
                              HttpSession session,
                              Model model) {
        
        String auth = email + ":" + token;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        
        String apiUrl = baseUrl.replaceAll("/+$", "") + "/rest/api/3/myself";
        
        try {
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = rt.exchange(apiUrl, HttpMethod.GET, entity, String.class);
            
            HttpHeaders responseHeaders = resp.getHeaders();
            String loginReason = responseHeaders.getFirst("X-Seraph-Loginreason");
            
            if (loginReason != null && loginReason.equals("AUTHENTICATED_FAILED")) {
                logger.error("JIRA authentication failed - X-Seraph-Loginreason: AUTHENTICATED_FAILED");
                model.addAttribute("error", "Authentication failed due to credentials provided are invalid.");
                return "login";
            }
            
        } catch (Exception e) {
            logger.error("Failed to authenticate with Jira: {}", e.getMessage());
            model.addAttribute("error", "Authentication failed: " + e.getMessage());
            return "login";
        }
        
        JiraCredentials creds = new JiraCredentials(baseUrl, email, token);
        session.setAttribute("jriCreds", creds);
        return "redirect:/timelog";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}