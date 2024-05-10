package com.yodist.inquiryiqbe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hw.serpapi.GoogleSearch;
import com.yodist.inquiryiqbe.dto.RelatedQuestion;
import com.yodist.inquiryiqbe.service.TraceService;
import com.yodist.inquiryiqbe.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
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

    @Autowired
    private TraceService traceService;

    @GetMapping
    public ResponseEntity<Object> getRelatedQuestions(@RequestParam("keyword") String keyword,
                                                      @RequestParam("language") String language,
                                                      @RequestParam("country") String country) {
        final Map<String, Object> responseBody = ResponseBuilder.generateDefaultResponseBody(traceService.getCurrentTraceId());
        try {
            log.info("in method getRelatedQuestions");

            Map<String, String> parameter = new HashMap<>();
            log.debug("serpapi.key: {}", serpapiKey);

            parameter.put("q", keyword);
            parameter.put("google_domain", "google.com");
            parameter.put("hl", language);
            parameter.put("gl", country);
            parameter.put("api_key", serpapiKey);

            List<RelatedQuestion> questionList = new ArrayList<>();
            JsonObject jsonObject;
            if ("production".equals(serpapiEnv)) {
                GoogleSearch search = new GoogleSearch(parameter);
                jsonObject = search.getJson();
            } else {
                String sampleJson = getSampleJson();
                jsonObject = JsonParser.parseString(sampleJson).getAsJsonObject();
            }
            JsonArray jsonArray = jsonObject.getAsJsonArray("related_questions");
            for (JsonElement el : jsonArray) {
                JsonObject question = el.getAsJsonObject();
                String qName = question.get("question").getAsString();
                RelatedQuestion qDto = new RelatedQuestion();
                qDto.setQuestion(qName);
                questionList.add(qDto);
            }

            if ("production".equals(serpapiEnv)) {

            } else {
                // add dummy subquestion
                for (RelatedQuestion q : questionList) {
                    List<RelatedQuestion> dummySubQuestionList = new ArrayList<>();
                    for (RelatedQuestion sq : questionList) {
                        RelatedQuestion subQuestion = new RelatedQuestion();
                        subQuestion.setQuestion(sq.getQuestion());
                        dummySubQuestionList.add(subQuestion);
                    }
                    q.setSubQuestions(dummySubQuestionList);
                }
            }

            responseBody.put("data", questionList);
            responseBody.put("message", "everything is alright");
            log.debug(objectMapper.writeValueAsString(responseBody));
        } catch (Exception ex) {
            log.error("error when trying to get related question data: {}", ex.getMessage());
            responseBody.put("errorMessage", "something is wrong!");
            return ResponseEntity.internalServerError().body(responseBody);
        }
        return ResponseEntity.ok(responseBody);
    }

    private static String getSampleJson() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("data/response-sample1.json");
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = classPathResource.getInputStream();
        for (int ch; (ch = inputStream.read()) != -1; ) {
            sb.append((char) ch);
        }
        return sb.toString();
    }
}
