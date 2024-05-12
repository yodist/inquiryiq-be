package com.yodist.inquiryiqbe.controller;

import com.yodist.inquiryiqbe.dto.request.RelatedQuestionReq;
import com.yodist.inquiryiqbe.service.RelatedQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/related-question")
public class RelatedQuestionController {

    private static final Logger log = LoggerFactory.getLogger(RelatedQuestionController.class);

    @Autowired
    private RelatedQuestionService relatedQuestionService;

    @GetMapping("/")
    public ResponseEntity<Object> getRelatedQuestions(@RequestParam("keyword") String keyword,
                                                      @RequestParam("language") String language,
                                                      @RequestParam("country") String country) {
        RelatedQuestionReq req = new RelatedQuestionReq();
        req.setKeyword(keyword);
        req.setLanguage(language);
        req.setCountry(country);
        return relatedQuestionService.getRelatedQuestions(req);
    }
}
