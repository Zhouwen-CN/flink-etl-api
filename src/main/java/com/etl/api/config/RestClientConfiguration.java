package com.etl.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                // .observationConvention(new DefaultClientRequestObservationConvention("flink.api.requests"))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic ZmxpbmthZG1pbjpBdSEyUGojQU1wc2M=")
                .build();
    }
}
