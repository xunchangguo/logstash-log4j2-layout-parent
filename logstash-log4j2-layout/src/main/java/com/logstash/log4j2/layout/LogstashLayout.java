package com.logstash.log4j2.layout;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logstash.log4j2.layout.renderer.TemplateRenderer;
import com.logstash.log4j2.layout.resolver.ContextDataResolver;
import com.logstash.log4j2.layout.resolver.ContextStackResolver;
import com.logstash.log4j2.layout.resolver.ExceptionClassNameResolver;
import com.logstash.log4j2.layout.resolver.ExceptionMessageResolver;
import com.logstash.log4j2.layout.resolver.ExceptionRootCauseClassNameResolver;
import com.logstash.log4j2.layout.resolver.ExceptionRootCauseMessageResolver;
import com.logstash.log4j2.layout.resolver.ExceptionRootCauseStackTraceResolver;
import com.logstash.log4j2.layout.resolver.ExceptionStackTraceResolver;
import com.logstash.log4j2.layout.resolver.LevelResolver;
import com.logstash.log4j2.layout.resolver.LoggerNameResolver;
import com.logstash.log4j2.layout.resolver.MessageResolver;
import com.logstash.log4j2.layout.resolver.SourceClassNameResolver;
import com.logstash.log4j2.layout.resolver.SourceFileNameResolver;
import com.logstash.log4j2.layout.resolver.SourceLineNumberResolver;
import com.logstash.log4j2.layout.resolver.SourceMethodNameResolver;
import com.logstash.log4j2.layout.resolver.TemplateResolver;
import com.logstash.log4j2.layout.resolver.TemplateResolverContext;
import com.logstash.log4j2.layout.resolver.ThreadNameResolver;
import com.logstash.log4j2.layout.resolver.TimestampResolver;
import com.logstash.log4j2.layout.util.Uris;

@Plugin(name = "LogstashLayout",
        category = Node.CATEGORY,
        elementType = Layout.ELEMENT_TYPE,
        printObject = true)
public class LogstashLayout extends AbstractStringLayout {

    private static final Set<TemplateResolver> RESOLVERS =
            Collections.unmodifiableSet(
                    new HashSet<TemplateResolver>(Arrays.asList(
                            ContextDataResolver.getInstance(),
                            ContextStackResolver.getInstance(),
                            ExceptionClassNameResolver.getInstance(),
                            ExceptionMessageResolver.getInstance(),
                            ExceptionRootCauseClassNameResolver.getInstance(),
                            ExceptionRootCauseMessageResolver.getInstance(),
                            ExceptionRootCauseStackTraceResolver.getInstance(),
                            ExceptionStackTraceResolver.getInstance(),
                            LevelResolver.getInstance(),
                            LoggerNameResolver.getInstance(),
                            MessageResolver.getInstance(),
                            SourceClassNameResolver.getInstance(),
                            SourceFileNameResolver.getInstance(),
                            SourceLineNumberResolver.getInstance(),
                            SourceMethodNameResolver.getInstance(),
                            ThreadNameResolver.getInstance(),
                            TimestampResolver.getInstance())));

    private final TemplateRenderer renderer;

    private LogstashLayout(Builder builder) {
        super(builder.config, StandardCharsets.UTF_8, null, null);
        String template = readTemplate(builder);
        FastDateFormat timestampFormat = readDateFormat(builder);
        ObjectMapper objectMapper = new ObjectMapper();
        StrSubstitutor substitutor = builder.config.getStrSubstitutor();
        TemplateResolverContext resolverContext = TemplateResolverContext
                .newBuilder()
                .setObjectMapper(objectMapper)
                .setTimestampFormat(timestampFormat)
                .setLocationInfoEnabled(builder.locationInfoEnabled)
                .setStackTraceEnabled(builder.stackTraceEnabled)
                .setEmptyPropertyExclusionEnabled(builder.emptyPropertyExclusionEnabled)
                .setMdcKeyPattern(builder.mdcKeyPattern)
                .setNdcPattern(builder.ndcPattern)
                .build();
        this.renderer = TemplateRenderer
                .newBuilder()
                .setSubstitutor(substitutor)
                .setResolverContext(resolverContext)
                .setPrettyPrintEnabled(builder.prettyPrintEnabled)
                .setTemplate(template)
                .setResolvers(RESOLVERS)
                .build();
    }

    private static String readTemplate(Builder builder) {
        return StringUtils.isBlank(builder.template)
                ? Uris.readUri(builder.templateUri)
                : builder.template;
    }

    private static FastDateFormat readDateFormat(Builder builder) {
        TimeZone timeZone = TimeZone.getTimeZone(builder.timeZoneId);
        return FastDateFormat.getInstance(builder.dateTimeFormatPattern, timeZone);
    }

    public String toSerializable(LogEvent event) {
        return renderer.render(event);
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<LogstashLayout> {

        @PluginConfiguration
        private Configuration config;

        @PluginBuilderAttribute
        private boolean prettyPrintEnabled = false;

        @PluginBuilderAttribute
        private boolean locationInfoEnabled = false;

        @PluginBuilderAttribute
        private boolean stackTraceEnabled = false;

        @PluginBuilderAttribute
        private boolean emptyPropertyExclusionEnabled = true;

        @PluginBuilderAttribute
        private String dateTimeFormatPattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";

        @PluginBuilderAttribute
        private String timeZoneId = TimeZone.getDefault().getID();

        @PluginBuilderAttribute
        private String template = null;

        @PluginBuilderAttribute
        private String templateUri = "classpath:LogstashJsonEventLayout.json";

        @PluginBuilderAttribute
        private String mdcKeyPattern;

        @PluginBuilderAttribute
        private String ndcPattern;

        private Builder() {
            // Do nothing.
        }

        public Configuration getConfiguration() {
            return config;
        }

        public Builder setConfiguration(Configuration configuration) {
            this.config = configuration;
            return this;
        }

        public boolean isPrettyPrintEnabled() {
            return prettyPrintEnabled;
        }

        public Builder setPrettyPrintEnabled(boolean prettyPrintEnabled) {
            this.prettyPrintEnabled = prettyPrintEnabled;
            return this;
        }

        public boolean isLocationInfoEnabled() {
            return locationInfoEnabled;
        }

        public Builder setLocationInfoEnabled(boolean locationInfoEnabled) {
            this.locationInfoEnabled = locationInfoEnabled;
            return this;
        }

        public boolean isStackTraceEnabled() {
            return stackTraceEnabled;
        }

        public Builder setStackTraceEnabled(boolean stackTraceEnabled) {
            this.stackTraceEnabled = stackTraceEnabled;
            return this;
        }

        public boolean isEmptyPropertyExclusionEnabled() {
            return emptyPropertyExclusionEnabled;
        }

        public Builder setEmptyPropertyExclusionEnabled(boolean blankPropertyExclusionEnabled) {
            this.emptyPropertyExclusionEnabled = blankPropertyExclusionEnabled;
            return this;
        }

        public String getDateTimeFormatPattern() {
            return dateTimeFormatPattern;
        }

        public Builder setDateTimeFormatPattern(String dateTimeFormatPattern) {
            this.dateTimeFormatPattern = dateTimeFormatPattern;
            return this;
        }

        public String getTimeZoneId() {
            return timeZoneId;
        }

        public Builder setTimeZoneId(String timeZoneId) {
            this.timeZoneId = timeZoneId;
            return this;
        }

        public String getTemplate() {
            return template;
        }

        public Builder setTemplate(String template) {
            this.template = template;
            return this;
        }

        public String getTemplateUri() {
            return templateUri;
        }

        public Builder setTemplateUri(String templateUri) {
            this.templateUri = templateUri;
            return this;
        }

        public String getMdcKeyPattern() {
            return mdcKeyPattern;
        }

        public Builder setMdcKeyPattern(String mdcKeyPattern) {
            this.mdcKeyPattern = mdcKeyPattern;
            return this;
        }

        public String getNdcPattern() {
            return ndcPattern;
        }

        public Builder setNdcPattern(String ndcPattern) {
            this.ndcPattern = ndcPattern;
            return this;
        }

        @Override
        public LogstashLayout build() {
            validate();
            return new LogstashLayout(this);
        }

        private void validate() {
            Validate.notNull(config, "config");
            Validate.notBlank(dateTimeFormatPattern, "dateTimeFormatPattern");
            Validate.notBlank(timeZoneId, "timeZoneId");
            Validate.isTrue(
                    !StringUtils.isBlank(template) || !StringUtils.isBlank(templateUri),
                    "both template and templateUri are blank");
        }

        @Override
        public String toString() {
            return "Builder{prettyPrintEnabled=" + prettyPrintEnabled +
                    ", locationInfoEnabled=" + locationInfoEnabled +
                    ", stackTraceEnabled=" + stackTraceEnabled +
                    ", emptyPropertyExclusionEnabled=" + emptyPropertyExclusionEnabled +
                    ", dateTimeFormatPattern='" + dateTimeFormatPattern + '\'' +
                    ", timeZoneId='" + timeZoneId + '\'' +
                    ", template='" + template + '\'' +
                    ", templateUri='" + templateUri + '\'' +
                    ", mdcKeyPattern='" + mdcKeyPattern + '\'' +
                    ", ndcPattern='" + ndcPattern + '\'' +
                    '}';
        }

    }

}
