package com.etl.api.service.manager;

import com.etl.api.domain.entity.Alert;
import com.etl.api.domain.entity.AlertJob;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.service.AlertJobService;
import com.etl.api.service.AlertService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.provider.FlinkApiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.etl.api.domain.entity.table.AlertJobTableDef.ALERT_JOB;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendMailManager {
    private final ObjectProvider<JavaMailSender> javaMailSender;
    private final AlertJobService alertJobService;
    private final AlertService alertService;
    private final EtlJobService etlJobService;
    private final FlinkClusterService flinkClusterService;
    private final FlinkApiProvider flinkApiProvider;
    @Value("${custom.send-mail.silent-time}")
    private Duration silentTime;
    @Value("${spring.mail.username}")
    private String form;


    public void send(EtlJobInstance etlJobInstance) {
        javaMailSender.ifAvailable(javaMailSender -> {
            // 获取 alert job 关系
            val jobId = etlJobInstance.getJobId();
            val alertJobMap = alertJobService.queryChain()
                    // sendTime 为空 || sendTime < now() - 静默时间
                    .where(ALERT_JOB.JOB_ID.eq(jobId)
                            .and(
                                    ALERT_JOB.SEND_TIME.isNull()
                                            .or(ALERT_JOB.SEND_TIME.lt(LocalDateTime.now().minus(silentTime.toMillis(), ChronoUnit.MILLIS)))
                            )
                    )
                    .list()
                    .stream()
                    .collect(Collectors.toMap(AlertJob::getAlertId, item -> item));
            if (alertJobMap.isEmpty()) {
                return;
            }

            // 获取 etl 任务
            val etlJob = etlJobService.queryChain()
                    .eq(EtlJob::getId, jobId)
                    .one();
            if (etlJob == null) {
                return;
            }

            // 获取 flink 集群
            val flinkCluster = flinkClusterService.queryChain()
                    .eq(FlinkCluster::getId, etlJob.getClusterId())
                    .one();
            if (flinkCluster == null) {
                return;
            }

            // 获取 alert 列表
            val alertList = alertService.queryChain()
                    .in(Alert::getId, alertJobMap.keySet())
                    .list();

            // 待更新的 alertJob sendTime
            val updateAlertJobList = new ArrayList<AlertJob>();

            for (Alert alert : alertList) {
                val name = alert.getName();
                val email = alert.getEmail();
                val alertJob = alertJobMap.get(alert.getId());
                val exception = flinkApiProvider.getJobException(flinkCluster.getJobManagerUrl(), etlJobInstance.getId());

                // 异常信息不为空
                if (StringUtils.hasText(exception)) {

                    // 更新 sendTime
                    alertJob.setSendTime(LocalDateTime.now());
                    updateAlertJobList.add(alertJob);

                    this.send(
                            javaMailSender,
                            name,
                            """
                                        <pre>
                                        <strong>%s</strong> 任务异常
                                    
                                        %s
                                        </pre>
                                    """.formatted(etlJob.getName(), exception),
                            email
                    );
                }
            }

            if (!updateAlertJobList.isEmpty()) {
                alertJobService.updateBatch(updateAlertJobList);
            }
        });


    }

    public void sendTest(String title, String email) {
        javaMailSender.ifAvailable(javaMailSender ->
                this.send(javaMailSender, title, "这是一封测试邮件", email)
        );
    }

    private void send(JavaMailSender javaMailSender, String subject, String text, String email) {
        // 创建一个邮件消息
        val message = javaMailSender.createMimeMessage();

        try {
            // 创建 MimeMessageHelper
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            // 发件人邮箱和名称
            helper.setFrom(form, "Flink-ETL-Platform");
            // 收件人邮箱
            helper.setTo(email);
            // 邮件标题
            helper.setSubject(subject);
            // 邮件正文，第二个参数表示是否是HTML正文
            helper.setText(text, true);
            // 发送
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件告警发送失败: {}", e.getMessage());
        }

        log.info("邮件发送成: {}", email);
    }
}
