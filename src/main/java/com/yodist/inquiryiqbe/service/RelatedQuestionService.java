package com.yodist.inquiryiqbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hw.serpapi.GoogleSearch;
import com.yodist.inquiryiqbe.dto.RelatedQuestion;
import com.yodist.inquiryiqbe.dto.request.RelatedQuestionReq;
import com.yodist.inquiryiqbe.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatedQuestionService {

    private static final Logger log = LoggerFactory.getLogger(RelatedQuestionService.class);

    @Value("${serpapi.key:test}")
    String serpapiKey;
    @Value("${serpapi.uri:https://serpapi.com/search}")
    String serpapiUri;
    @Value("${serpapi.env:dummy}")
    String serpapiEnv;
    @Value("${google.domain:google.com}")
    String googleDomain;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TraceService traceService;

    public ResponseEntity<Object> getRelatedQuestions(RelatedQuestionReq req) {
        final Map<String, Object> responseBody = ResponseBuilder.generateDefaultResponseBody(traceService.getCurrentTraceId());
        try {
            log.info("in method getRelatedQuestions");

            Map<String, String> parameter = new HashMap<>();
            parameter.put("q", req.getKeyword());
            parameter.put("google_domain", googleDomain);
            parameter.put("hl", req.getLanguage());
            parameter.put("gl", req.getCountry());
            parameter.put("api_key", serpapiKey);
            log.debug("serpapi.key: {}", serpapiKey);

            JsonObject jsonObject;
            if (isProduction()) {
                jsonObject = getJsonObjectFromGoogle(parameter);
            } else {
                jsonObject = getJsonObjectFromDummyFile();
            }
            List<RelatedQuestion> questionList = getRelatedQuestionsFromJsonObject(jsonObject);
            setDummySubQuestion(questionList);

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

    private static List<RelatedQuestion> getRelatedQuestionsFromJsonObject(JsonObject jsonObject) {
        List<RelatedQuestion> questionList = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("related_questions");
        for (JsonElement el : jsonArray) {
            JsonObject question = el.getAsJsonObject();
            String qName = question.get("question").getAsString();
            RelatedQuestion qDto = new RelatedQuestion();
            qDto.setQuestion(qName);
            questionList.add(qDto);
        }
        return questionList;
    }

    private static JsonObject getJsonObjectFromDummyFile() throws IOException {
        JsonObject jsonObject;
        String sampleJson = getSampleJson();
        jsonObject = JsonParser.parseString(sampleJson).getAsJsonObject();
        return jsonObject;
    }

    private static JsonObject getJsonObjectFromGoogle(Map<String, String> parameter) {
        JsonObject jsonObject;
        GoogleSearch search = new GoogleSearch(parameter);
        jsonObject = search.getJson();
        return jsonObject;
    }

    private void setDummySubQuestion(final List<RelatedQuestion> questionList) {
        if (!isProduction()) {
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
    }

    private boolean isProduction() {
        return "production".equals(serpapiEnv);
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
