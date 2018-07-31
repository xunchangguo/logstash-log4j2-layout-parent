package com.logstash.log4j2.layout.resolver;

import org.apache.logging.log4j.core.LogEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.logstash.log4j2.layout.util.Throwables;

public class ExceptionStackTraceResolver implements TemplateResolver {

    private static final ExceptionStackTraceResolver INSTANCE = new ExceptionStackTraceResolver();

    private ExceptionStackTraceResolver() {
        // Do nothing.
    }

    public static ExceptionStackTraceResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "exceptionStackTrace";
    }

    @Override
    public JsonNode resolve(TemplateResolverContext context, LogEvent logEvent, String key) {
        Throwable exception = logEvent.getThrown();
        if (!context.isStackTraceEnabled() || exception == null) {
            return NullNode.getInstance();
        }
        String exceptionStackTrace = Throwables.serializeStackTrace(exception);
        return new TextNode(exceptionStackTrace);
    }

}
