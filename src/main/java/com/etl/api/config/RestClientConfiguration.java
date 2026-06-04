package com.etl.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .observationConvention(new DefaultClientRequestObservationConvention("flink.api.requests"))
                .build();
    }
}
