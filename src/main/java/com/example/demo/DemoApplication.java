package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            try {
                // --- CONFIGURATION ---
                String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
                String submitUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

                // --- STEP 1: PREPARE USER DATA ---
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("name", "K Sindhu"); 
                requestBody.put("regNo", "22BCE8991"); 
                requestBody.put("email", "sindhu.22bce8991@vitapstudent.ac.in"); 

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

                // --- STEP 2: GET TOKEN ---
                System.out.println("1. Requesting Token...");
                ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, request, Map.class);
                Map<String, Object> responseBody = response.getBody();
                
                String accessToken = (String) responseBody.get("accessToken");
                System.out.println("   Token Received: " + accessToken);

                // --- STEP 3: THE SQL QUERY (QUESTION 1 SOLUTION) ---
                String sqlSolution = 
                    "SELECT " +
                    "    t.DEPARTMENT_NAME, " +
                    "    t.TotalSalary as SALARY, " +
                    "    t.EMPLOYEE_NAME, " +
                    "    t.AGE " +
                    "FROM ( " +
                    "    SELECT " +
                    "        d.DEPARTMENT_NAME, " +
                    "        SUM(p.AMOUNT) as TotalSalary, " +
                    "        CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as EMPLOYEE_NAME, " +
                    "        TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) as AGE, " +
                    "        RANK() OVER (PARTITION BY d.DEPARTMENT_ID ORDER BY SUM(p.AMOUNT) DESC) as rnk " +
                    "    FROM EMPLOYEE e " +
                    "    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                    "    JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
                    "    WHERE DAY(p.PAYMENT_TIME) <> 1 " + 
                    "    GROUP BY e.EMP_ID, d.DEPARTMENT_ID, d.DEPARTMENT_NAME, e.FIRST_NAME, e.LAST_NAME, e.DOB " +
                    ") t " +
                    "WHERE t.rnk = 1"; 

                // --- STEP 4: SUBMIT SOLUTION ---
                Map<String, String> solutionBody = new HashMap<>();
                solutionBody.put("finalQuery", sqlSolution);

                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.setContentType(MediaType.APPLICATION_JSON);
                authHeaders.set("Authorization", accessToken);

                HttpEntity<Map<String, String>> solutionRequest = new HttpEntity<>(solutionBody, authHeaders);

                System.out.println("2. Submitting SQL Solution...");
                ResponseEntity<String> finalResponse = restTemplate.postForEntity(submitUrl, solutionRequest, String.class);
                
                System.out.println("--- SUCCESS! ---");
                System.out.println("Response Code: " + finalResponse.getStatusCode());
                System.out.println("Response Body: " + finalResponse.getBody());

            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}