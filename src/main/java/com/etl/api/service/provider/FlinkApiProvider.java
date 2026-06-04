package com.etl.api.service.provider;

import cn.hutool.core.codec.Base64;
import com.etl.api.exception.FlinkApiRequestException;
import com.etl.api.scheduler.SyncJobInstanceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
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
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [获取集群版本信息] 请求失败: " + e.getResponseBodyAsString());
        }

        return Optional.ofNullable(jsonNode)
                .map(item -> item.get("flink-version"))
                .map(JsonNode::asText)
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [获取集群版本信息] 解析version错误: " + jsonNode));
    }

    public String uploadJar(String jobManagerUrl, String path) {
        val builder = new MultipartBodyBuilder();
        builder.part("jarfile", new FileSystemResource(path));

        JsonNode jsonNode;
        try {
            jsonNode = restClient.post()
                    .uri(jobManagerUrl + "/jars/upload")
                    .body(builder.build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [上传jar包] 请求失败: " + e.getResponseBodyAsString());
        }

        return Optional.ofNullable(jsonNode)
                .map(item -> item.get("filename"))
                .map(JsonNode::asText)
                .map(item -> item.substring(item.lastIndexOf("/") + 1))
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [上传jar包] 解析jarId错误: " + jsonNode));
    }

    public String runJob(String jobManagerUrl, String jarId, String mainClass, String config, @Nullable String savePointPath) {
        val body = new HashMap<String, String>();
        body.put("entryClass", mainClass);
        body.put("programArgs", "--config " + Base64.encode(config));
        body.put("savepointPath", savePointPath);

        JsonNode jsonNode;
        try {
            jsonNode = restClient.post()
                    .uri(jobManagerUrl + "/jars/" + jarId + "/run")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [启动任务] 请求失败: " + e.getResponseBodyAsString());
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
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [获取任务状态] 请求失败: " + e.getResponseBodyAsString());
        }


        return Optional.ofNullable(jsonNode)
                .filter(item -> item.get("jid") != null)
                .map(item -> objectMapper.convertValue(item, SyncJobInstanceStatus.FlinkJobStatusDTO.class))
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [获取任务状态] 解析数据错误: " + jsonNode));
    }


    public void cancelJob(String jobManagerUrl, String flinkJobId) {
        try {
            restClient.post()
                    .uri(jobManagerUrl + "/jobs/" + flinkJobId + "/stop")
                    .retrieve()
                    .body(Void.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [停止任务] 请求失败: " + e.getResponseBodyAsString());
        }
    }

    public List<CheckpointHistoryDTO> getCheckpointHistory(String jobManagerUrl, String flinkJobId) {
        JsonNode jsonNode;
        try {
            jsonNode = restClient.get()
                    .uri(jobManagerUrl + "/jobs/" + flinkJobId + "/checkpoints")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [获取检查点历史] 请求失败" + e.getResponseBodyAsString());
        }

        return Optional.ofNullable(jsonNode)
                .map(item -> item.get("history"))
                .filter(JsonNode::isArray)
                .map(item -> objectMapper.convertValue(item, new TypeReference<List<CheckpointHistoryDTO>>() {
                }))
                .orElseThrow(() -> new FlinkApiRequestException("Flink API [获取检查点历史] 数据解析错误: " + jsonNode));
    }

    public void deleteJar(String jobManagerUrl, String jarId) {
        try {
            restClient.delete()
                    .uri(jobManagerUrl + "/jars/" + jarId)
                    .retrieve()
                    .body(Void.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [删除jar包] 请求失败: " + e.getResponseBodyAsString());
        }
    }

    @Getter
    @Setter
    public static class CheckpointHistoryDTO {
        @JsonProperty("status")
        private String status;
        @JsonProperty("is_savepoint")
        private Boolean savepoint;
        @JsonProperty("trigger_timestamp")
        private Long triggerTimestamp;
        @JsonProperty("external_path")
        private String externalPath;
    }
}
