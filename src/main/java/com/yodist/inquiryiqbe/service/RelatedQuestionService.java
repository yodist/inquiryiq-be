package com.yodist.inquiryiqbe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.hw.serpapi.GoogleSearch;
import com.yodist.inquiryiqbe.dto.RelatedQuestion;
import com.yodist.inquiryiqbe.dto.request.RelatedQuestionReq;
import com.yodist.inquiryiqbe.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yodist.inquiryiqbe.constant.Constant.*;

@Service
public class RelatedQuestionService {

    private static final Logger log = LoggerFactory.getLogger(RelatedQuestionService.class);

    @Value("${serpapi.key:test}")
    String serpapiKey;
    // valid values are "production" and "nonprod"
    @Value("${data.env:nonprod}")
    String dataEnv;
    @Value("${google.domain:google.com}")
    String googleDomain;
    @Value("${serphouse.url:https://api.serphouse.com/serp/live}")
    String serphouseUrl;
    @Value("${serphouse.key:test}")
    String serphouseKey;
    @Value("${serp.engine:serphouse}")
    String serpEngine;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TraceService traceService;

    public ResponseEntity<Object> getRelatedQuestions(RelatedQuestionReq req) {
        final Map<String, Object> responseBody = ResponseBuilder.generateDefaultResponseBody(traceService.getCurrentTraceId());
        try {
            log.info("in method getRelatedQuestions");

            List<RelatedQuestion> questionList = new ArrayList<>();

            if (SERPAPI.equals(serpEngine)) {
                log.info("use SERPAPI");
                JsonObject jsonObject;
                if (isProduction()) {
                    Map<String, String> parameter = new HashMap<>();
                    parameter.put(SERPAPI_Q, req.getKeyword());
                    parameter.put(SERPAPI_GOOGLE_DOMAIN, googleDomain);
                    parameter.put(SERPAPI_HL, req.getLanguage());
                    parameter.put(SERPAPI_GL, req.getCountry());
                    parameter.put(SERPAPI_API_KEY, serpapiKey);
                    log.debug("serpapi.key: {}", serpapiKey);
                    jsonObject = getJsonObjectFromGoogleSerpapi(parameter);
                } else {
                    jsonObject = getJsonObjectFromDummyFile();
                }
                questionList = getRelatedQuestionsFromJsonObject(jsonObject);
                setDummySubQuestion(questionList);
            } else if (SERPHOUSE.equals(serpEngine)) {
                log.info("use SERPHOUSE");
                JsonObject jsonObject;
                if (isProduction()) {
                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put(SERPHOUSE_Q, req.getKeyword());
                    requestData.put(SERPHOUSE_DOMAIN, googleDomain);
                    requestData.put(SERPHOUSE_LOC, req.getCountry());
                    requestData.put(SERPHOUSE_LANG, req.getLanguage());
                    requestData.put(SERPHOUSE_DEVICE, "desktop");
                    requestData.put(SERPHOUSE_SERPTYPE, "web");
                    requestData.put(SERPHOUSE_PAGE, "1");
                    requestData.put(SERPHOUSE_VERBATIM, "0");
                    requestData.put(SERPHOUSE_NUM_RESULT, "10");
                    Map<String, Object> wrapper = new HashMap<>();
                    wrapper.put("data", requestData);
                    // Set headers with Content-Type
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(serphouseKey);
                    String wrapperString = objectMapper.writeValueAsString(wrapper);
                    // Create a HttpEntity with the request data and headers
                    HttpEntity<String> request = new HttpEntity<>(wrapperString, headers);
                    log.debug(wrapperString);
                    // Send the POST request
                    ResponseEntity<String> response = restTemplate.postForEntity(serphouseUrl, request, String.class);
                    log.debug(String.valueOf(response));
                    jsonObject = getJsonObjectFromGoogleSerphouse(response.getBody());
                } else {
                    jsonObject = getJsonObjectFromDummyFile();
                }
                questionList = getRelatedQuestionsFromJsonObjectGoogleSerphouse(jsonObject);
            } else {
                // TODO: will need to implement this, get data directly from google and bypass captcha
                JsonObject jsonObject = getJsonObjectFromGoogle(null);
                log.debug("getJsonObjectFromGoogle {}", jsonObject);
            }

            responseBody.put(BODY_DATA, questionList);
            responseBody.put(BODY_MESSAGE, "everything is alright");
            log.debug(objectMapper.writeValueAsString(responseBody));
        } catch (Exception ex) {
            log.error("error when trying to get related question data: {}", ex.getMessage());
            log.trace("error", ex);
            responseBody.put(BODY_ERROR_MESSAGE, "something is wrong!");
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

    private static JsonObject getJsonObjectFromGoogleSerpapi(Map<String, String> parameter) {
        JsonObject jsonObject;
        GoogleSearch search = new GoogleSearch(parameter);
        jsonObject = search.getJson();
        return jsonObject;
    }

    private static JsonObject getJsonObjectFromGoogleSerphouse(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, JsonObject.class);
    }

    private static List<RelatedQuestion> getRelatedQuestionsFromJsonObjectGoogleSerphouse(JsonObject jsonObject) {
        List<RelatedQuestion> questionList = new ArrayList<>();
        JsonObject results = jsonObject.getAsJsonObject("results").getAsJsonObject("results");
        JsonArray peopleAlsoAskList = results.getAsJsonArray("people_also_ask");
        for (JsonElement el : peopleAlsoAskList) {
            RelatedQuestion question = new RelatedQuestion();
            String questionValue = el.getAsJsonObject().get("question").getAsString();
            question.setQuestion(questionValue);
            questionList.add(question);
        }
        return questionList;
    }

    private JsonObject getJsonObjectFromGoogle(Map<String, String> parameter) {
        return new JsonObject();
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
        return ENV_PRODUCTION.equals(dataEnv);
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
