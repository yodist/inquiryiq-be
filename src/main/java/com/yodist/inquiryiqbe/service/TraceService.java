package com.yodist.inquiryiqbe.service;

import brave.Span;
import brave.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TraceService {

    @Autowired
    Tracer tracer;

    public String getCurrentTraceId() {
        Span span = tracer.currentSpan();
        return span.context().traceIdString();
    }

}
