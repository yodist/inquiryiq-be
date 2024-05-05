package com.yodist.inquiryiqbe.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {
    public static Map<String, Object> generateDefaultResponseBody() {
        final Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("traceId", "MDC");
        responseBody.put("dateTime", new Date());
        return responseBody;
    }
}
