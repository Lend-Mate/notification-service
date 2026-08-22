package com.lendmate.notificationservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignTracingConfig {
    @Bean
    public RequestInterceptor otelFeignRequestInterceptor(OpenTelemetry openTelemetry) {
        TextMapSetter<RequestTemplate> setter =
                (carrier, key, value) -> carrier.header(key, value);

        return requestTemplate -> openTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), requestTemplate, setter);
    }
}
