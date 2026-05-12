package com.etl.api;

import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.StandaloneClusterDescriptor;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.client.program.ProgramInvocationException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;

import java.io.File;

public class FlinkClientTest2 {
    public static void main(String[] args) throws ProgramInvocationException {
        // 构建 Application 配置
        ApplicationConfiguration appConfig = new ApplicationConfiguration(
                new String[]{
                        "--config", """
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
                        """
                },
                "com.etl.client.EtlClient"   // 你的 main 类
        );


        Configuration config = new Configuration();
        config.set(RestOptions.ADDRESS, "172.23.108.25");
        config.set(RestOptions.PORT, 8081);

        // 创建 Standalone 部署描述符
        StandaloneClusterDescriptor descriptor = new StandaloneClusterDescriptor(config);

        ClusterSpecification clusterSpecification = new ClusterSpecification.ClusterSpecificationBuilder()
                .createClusterSpecification();
        // 部署 Application 集群（实际上是提交到已存在的集群）
        ClusterClient<StandaloneClusterId> client = descriptor.deployApplicationCluster(clusterSpecification, appConfig).getClusterClient();


        PackagedProgram packagedProgram = PackagedProgram.newBuilder()
                .setJarFile(new File("D:\\work\\idea\\flink-etl-tool\\flink-etl-client\\target\\flink-etl-client-1.0.0-SNAPSHOT.jar"))
                .setEntryPointClassName("com.etl.client.EtlClient")
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
                )*/
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
        client.submitJob(jobGraph);

        descriptor.close();
    }
}
