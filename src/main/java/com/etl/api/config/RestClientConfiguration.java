package com.etl.api.config;

import com.etl.api.exception.RestClientRequestStatusException;
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
                .defaultStatusHandler(httpStatusCode -> !httpStatusCode.is2xxSuccessful(), (request, response) -> {
                    throw new RestClientRequestStatusException(request);
                })
                .build();
    }
}
