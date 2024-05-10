package com.yodist.inquiryiqbe.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class RelatedQuestion {
    private String question;
    private List<RelatedQuestion> subQuestions;
}
