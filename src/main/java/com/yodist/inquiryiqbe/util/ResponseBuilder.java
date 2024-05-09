package com.yodist.inquiryiqbe.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {
    public static Map<String, Object> generateDefaultResponseBody(String traceId) {
        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("trace_id", traceId);
        responseBody.put("date_time", new Date());
        return responseBody;
    }
}
