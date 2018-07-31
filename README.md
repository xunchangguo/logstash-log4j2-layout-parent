# logstash-log4j2-layout-parent
Log4j 2.x plugin for Logstash-friendly JSON layout. from https://github.com/vy/log4j2-logstash-layout

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="CONSOLE" target="SYSTEM_OUT">
            <LogstashLayout dateTimeFormatPattern="yyyy-MM-dd'T'HH:mm:ss.SSSZZZ"
                            templateUri="classpath:JsonEventLayout.json"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="CONSOLE"/>
        </Root>
    </Loggers>
</Configuration>
```

```json
{
  "exception": {
    "exception_class": "${json:exceptionClassName}",
    "exception_message": "${json:exceptionMessage}",
    "stacktrace": "${json:exceptionStackTrace}"
  },
  "msg": "${json:message}",
  "level": "${json:level}",
  "logger": "${json:loggerName}"
}
```

This generates an output as follows: 
```json
{"msg":"正常记录运行日志。","level":"INFO","logger":"com.chinacreator.c2.web.rest.LoggerTestResource"}
```
