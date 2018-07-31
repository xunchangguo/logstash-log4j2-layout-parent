package com.logstash.log4j2.layout.resolver;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.LogEvent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.RawValue;

public class MessageResolver implements TemplateResolver {

    private static final MessageResolver INSTANCE = new MessageResolver();

    private MessageResolver() {
        // Do nothing.
    }

    public static MessageResolver getInstance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return "message";
    }

    @Override
    public JsonNode resolve(TemplateResolverContext context, LogEvent logEvent, String key) {
        String message = logEvent.getMessage().getFormattedMessage();
        boolean messageExcluded = StringUtils.isEmpty(message) && context.isEmptyPropertyExclusionEnabled();
        if(messageExcluded) {
        	return NullNode.getInstance();
        }else {
        	if(mayBeJSON(message)) {
        		return new POJONode(new RawValue(message));
        	}else{
        		return new TextNode(message);
        	}
        }
//        return messageExcluded
//                ? NullNode.getInstance()
//                : new TextNode(message);
    }
    
    public static boolean mayBeJSON( String string ) {
        return string != null
              && ("null".equals( string )
                    || (string.startsWith( "[" ) && string.endsWith( "]" )) || (string.startsWith( "{" ) && string.endsWith( "}" )));
     }

}
