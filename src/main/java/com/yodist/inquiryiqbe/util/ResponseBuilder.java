package com.yodist.inquiryiqbe.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {
    public static Map<String, Object> generateDefaultResponseBody() {
        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("trace_id", "MDC");
        responseBody.put("date_time", new Date());
        return responseBody;
    }
}
