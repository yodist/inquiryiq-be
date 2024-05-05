package com.yodist.inquiryiqbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hw.serpapi.GoogleSearch;
import com.yodist.inquiryiqbe.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/related-question")
public class RelatedQuestionController {

    private static final Logger log = LoggerFactory.getLogger(RelatedQuestionController.class);
    @Value("${serpapi.key:test}")
    String serpapiKey;
    @Value("${serpapi.uri:https://serpapi.com/search}")
    String serpapiUri;
    @Value("${serpapi.env:dummy}")
    String serpapiEnv;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Object> getRelatedQuestions() {
        final Map<String, Object> responseBody = ResponseBuilder.generateDefaultResponseBody();
        try {
            log.info("in method getRelatedQuestions");

            Map<String, String> parameter = new HashMap<>();
            log.debug("serpapi.key: {}", serpapiKey);

            parameter.put("q", "Benefits of Tea");
            parameter.put("location", "Austin, Texas, United States");
            parameter.put("google_domain", "google.com");
            parameter.put("hl", "en");
            parameter.put("gl", "us");
            parameter.put("api_key", serpapiKey);

            if ("production".equals(serpapiEnv)) {
                // Create search
                GoogleSearch search = new GoogleSearch(parameter);

                JsonObject result = search.getJson();

//                String searchResult = result.getAsJsonArray("organic_results")
//                        .get(0)
//                        .getAsJsonObject()
//                        .get("snippet")
//                        .getAsString();
            } else {

            }
            responseBody.put("message", "everything is alright");
            log.debug(objectMapper.writeValueAsString(responseBody));
        } catch (Exception ex) {
            log.error("error when trying to get related question data: {}", ex.getMessage());
            responseBody.put("errorMessage", "something is wrong!");
            return ResponseEntity.internalServerError().body(responseBody);
        }
        return ResponseEntity.ok(responseBody);
    }
}
