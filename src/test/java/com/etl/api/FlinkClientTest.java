/*
package com.etl.api;

import lombok.val;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.rest.messages.job.JobDetailsInfo;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FlinkClientTest {
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        config.set(RestOptions.ADDRESS, "172.23.108.25");
        config.set(RestOptions.PORT, 8081);

        RestClusterClient<StandaloneClusterId> client = new RestClusterClient<>(config, StandaloneClusterId.getInstance());

        // cluster id
        StandaloneClusterId clusterId = client.getClusterId();
        System.out.println("clusterId = " + clusterId);

        // web url
        val webInterfaceURL = client.getWebInterfaceURL();
        System.out.println("webInterfaceURL = " + webInterfaceURL);

        // config
        Configuration flinkConfiguration = client.getFlinkConfiguration();
        System.out.println("flinkConfiguration = " + flinkConfiguration);

        // submit
        PackagedProgram packagedProgram = PackagedProgram.newBuilder()
                .setJarFile(new File("D:\\work\\idea\\flink-etl-tool\\flink-etl-client\\target\\flink-etl-client-1.0.0-SNAPSHOT.jar"))
                .setEntryPointClassName("com.etl.client.EtlClient")
                */
/*.setUserClassPaths(
                        List.of(
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-table-common\\1.15.2\\flink-table-common-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-table-api-java-bridge\\1.15.2\\flink-table-api-java-bridge-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-table-api-java\\1.15.2\\flink-table-api-java-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-table-runtime\\1.15.2\\flink-table-runtime-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-table-api-bridge-base\\1.15.2\\flink-table-api-bridge-base-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-table-planner-loader\\1.15.2\\flink-table-planner-loader-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-cep\\1.15.2\\flink-cep-1.15.2.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\com\\ververica\\flink-connector-debezium\\2.3.0\\flink-connector-debezium-2.3.0.jar"),
                                new URL("file:/D:\\env\\apache-maven-3.9.11\\repository\\org\\apache\\flink\\flink-connector-kafka\\1.15.2\\flink-connector-kafka-1.15.2.jar")
                        )
                )*//*

                .setArguments("--config", """
                        {
                          "job": {
                            "name": "csv-to-console",
                            "mode": "batch",
                            "parallelism": 1
                          },
                          "sources": [
                            {
                              "type": "localfile",
                              "outputTable": "csv_source",
                              "config": {
                                "path": "/root/user.csv",
                                "format": "csv",
                                "encoding": "UTF-8",
                                "delimiter": ",",
                                "skipHeader": true,
                                "schema": {
                                  "id": "LONG",
                                  "name": "STRING",
                                  "age": "INT",
                                  "email": "STRING"
                                }
                              }
                            }
                          ],
                          "sinks": [
                            {
                              "type": "console",
                              "inputTable": "csv_source"
                            }
                          ]
                        }
                        """)
                .build();

        JobGraph jobGraph = PackagedProgramUtils.createJobGraph(packagedProgram, new Configuration(), 1, false);
        CompletableFuture<JobID> jobIDCompletableFuture = client.submitJob(jobGraph);
        val jobID = jobIDCompletableFuture.get();
        System.out.println("jobID = " + jobID);

        // job detail
        CompletableFuture<JobDetailsInfo> jobDetails = client.getJobDetails(jobID);
        JobDetailsInfo jobDetailsInfo = jobDetails.get();
        System.out.println("jobDetailsInfo = " + jobDetailsInfo);

        // accumulators
        CompletableFuture<Map<String, Object>> accumulators = client.getAccumulators(jobID);
        Map<String, Object> stringObjectMap = accumulators.get();
        System.out.println("stringObjectMap = " + stringObjectMap);

        // job status
        CompletableFuture<JobStatus> jobStatus = client.getJobStatus(jobID);
        val jobStatus1 = jobStatus.get();
        System.out.println("jobStatus1 = " + jobStatus1);

        client.close();
    }
}
*/
