package com.logstash.log4j2.layout.resolver;

import org.apache.logging.log4j.core.LogEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.logstash.log4j2.layout.util.Throwables;

public class ExceptionRootCauseClassNameResolver implements TemplateResolver {

    private static final ExceptionRootCauseClassNameResolver INSTANCE = new ExceptionRootCauseClassNameResolver();

    private ExceptionRootCauseClassNameResolver() {
        // Do nothing.
    }

    public static ExceptionRootCauseClassNameResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "exceptionRootCauseClassName";
    }

    @Override
    public JsonNode resolve(TemplateResolverContext context, LogEvent logEvent, String key) {
        Throwable exception = logEvent.getThrown();
        if (exception == null) {
            return NullNode.getInstance();
        }
        Throwable rootCause = Throwables.getRootCause(exception);
        String rootCauseClassName = rootCause.getClass().getCanonicalName();
        return new TextNode(rootCauseClassName);
    }

}
