package com.logstash.log4j2.layout.resolver;

import org.apache.logging.log4j.core.LogEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.logstash.log4j2.layout.util.Throwables;

public class ExceptionRootCauseStackTraceResolver implements TemplateResolver {

    private static final ExceptionRootCauseStackTraceResolver INSTANCE = new ExceptionRootCauseStackTraceResolver();

    private ExceptionRootCauseStackTraceResolver() {
        // Do nothing.
    }

    public static ExceptionRootCauseStackTraceResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "exceptionRootCauseStackTrace";
    }

    @Override
    public JsonNode resolve(TemplateResolverContext context, LogEvent logEvent, String key) {
        Throwable exception = logEvent.getThrown();
        if (!context.isStackTraceEnabled() || exception == null) {
            return NullNode.getInstance();
        }
        Throwable rootCause = Throwables.getRootCause(exception);
        String exceptionStackTrace = Throwables.serializeStackTrace(rootCause);
        return new TextNode(exceptionStackTrace);
    }

}
