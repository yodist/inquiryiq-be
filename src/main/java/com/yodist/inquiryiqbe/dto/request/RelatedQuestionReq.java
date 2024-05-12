package com.yodist.inquiryiqbe.dto.request;

import lombok.Data;

@Data
public class RelatedQuestionReq {
    private String keyword;
    private String language;
    private String country;
}
