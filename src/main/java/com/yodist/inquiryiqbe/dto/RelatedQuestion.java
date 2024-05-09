package com.yodist.inquiryiqbe.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class RelatedQuestion {
    private String question;
    private RelatedQuestion subQuestion;
}
