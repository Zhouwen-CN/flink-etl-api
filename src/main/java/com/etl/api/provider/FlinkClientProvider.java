package com.etl.api.provider;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlinkClientProvider {

    private final RestClient restClient;

    public Optional<String> getVersion(String ip, Integer port) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.scheme("http").host(ip).port(port).path("/config").build())
                    .exchangeForRequiredValue((request, response) -> Optional.ofNullable(response.bodyTo(JsonNode.class))
                            .map(jsonNode -> jsonNode.get("flink-version"))
                            .map(JsonNode::asText)
                    );
        } catch (Exception e) {
            log.error("flink api get version error: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
