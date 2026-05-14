package com.etl.api.service.provider;

import cn.hutool.core.codec.Base64;
import com.etl.api.exception.FlinkApiRequestException;
import com.etl.api.scheduler.SyncJobInstanceStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlinkApiProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public String getVersion(String jobManagerUrl) {
        JsonNode jsonNode;
        try {
            jsonNode = restClient.get()
                    .uri(jobManagerUrl + "/config")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            throw new FlinkApiRequestException("Flink API [获取集群版本信息] 请求失败", e);
        }

        return Optional.ofNullable(jsonNode)
                .map(item -> item.get("flink-version"))
                .map(JsonNode::asText)
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [获取集群版本信息] 解析version错误: " + jsonNode));
    }

    public String uploadJar(String jobManagerUrl, String path, String jarId) {
        if (jarId != null) {
            try {
                restClient.delete()
                        .uri(jobManagerUrl + "/jars/" + jarId)
                        .retrieve()
                        .body(Void.class);
            } catch (Exception e) {
                throw new FlinkApiRequestException("Flink API [删除jar包] 请求失败", e);
            }
        }

        val builder = new MultipartBodyBuilder();
        builder.part("jarfile", new FileSystemResource(path));

        JsonNode jsonNode;
        try {
            jsonNode = restClient.post()
                    .uri(jobManagerUrl + "/jars/upload")
                    .body(builder.build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            throw new FlinkApiRequestException("Flink API [上传jar包] 请求失败", e);
        }

        return Optional.ofNullable(jsonNode)
                .map(item -> item.get("filename"))
                .map(JsonNode::asText)
                .map(item -> item.substring(item.lastIndexOf("/") + 1))
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [上传jar包] 解析jarId错误: " + jsonNode));
    }

    public String runJob(String jobManagerUrl, String jarId, String mainClass, String config) {
        val body = new HashMap<String, String>();
        body.put("entryClass", mainClass);
        body.put("programArgs", "--config " + Base64.encode(config));

        JsonNode jsonNode;
        try {
            jsonNode = restClient.post()
                    .uri(jobManagerUrl + "/jars/" + jarId + "/run")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            throw new FlinkApiRequestException("Flink API [启动任务] 请求失败", e);
        }

        return Optional.ofNullable(jsonNode)
                .map(item -> item.get("jobid"))
                .map(JsonNode::asText)
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [启动任务] 解析jobId错误: " + jsonNode));
    }

    public SyncJobInstanceStatus.FlinkJobStatusDTO getJobStatus(String jobManagerUrl, String jobId) {
        JsonNode jsonNode;
        try {
            jsonNode = restClient.get()
                    .uri(jobManagerUrl + "/jobs/" + jobId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            throw new FlinkApiRequestException("Flink API [获取任务状态] 请求失败", e);
        }


        return Optional.ofNullable(jsonNode)
                .filter(item -> item.get("jid") != null)
                .map(item -> objectMapper.convertValue(item, SyncJobInstanceStatus.FlinkJobStatusDTO.class))
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [获取任务状态] 解析数据错误: " + jsonNode));
    }
}
