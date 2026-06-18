package com.etl.api.service.provider;

import com.etl.api.exception.FlinkApiRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlinkApiProvider {

    private final RestClient restClient;

    // 获取版本信息
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

    // 上传jar包
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

    // 删除jar包
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

    // 获取jar包列表
    public JsonNode getJars(String jobManagerUrl) {
        try {
            return restClient.get()
                    .uri(jobManagerUrl + "/jars")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [删除jar包] 请求失败: " + e.getResponseBodyAsString());
        }
    }

    // 提交任务
    public String runJob(String jobManagerUrl, String jarId, String mainClass, String config, @Nullable String savePointPath) {
        val body = new HashMap<String, String>();

        body.put("entryClass", mainClass);
        body.put("programArgs", "--config " + Base64.getEncoder().encodeToString(config.getBytes(StandardCharsets.UTF_8)));
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

    // 获取任务状态
    public JsonNode getJobStatus(String jobManagerUrl, String jobId) {
        try {
            return restClient.get()
                    .uri(jobManagerUrl + "/jobs/" + jobId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [获取任务状态] 请求失败: " + e.getResponseBodyAsString());
        }
    }

    // 停止任务
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

    // 获取检查点列表
    public JsonNode getCheckpointHistory(String jobManagerUrl, String flinkJobId) {
        try {
            return restClient.get()
                    .uri(jobManagerUrl + "/jobs/" + flinkJobId + "/checkpoints")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException e) {
            throw new FlinkApiRequestException("Flink API [获取检查点历史] 请求失败" + e.getResponseBodyAsString());
        }
    }
}
