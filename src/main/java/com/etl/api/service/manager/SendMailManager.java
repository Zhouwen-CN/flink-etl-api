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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

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
            val jobId = etlJobInstance.getJobId();
            val etlJob = etlJobService.queryChain()
                    .eq(EtlJob::getId, jobId)
                    .one();

            val flinkCluster = flinkClusterService.queryChain()
                    .eq(FlinkCluster::getId, etlJob.getClusterId())
                    .one();

            val alertIds = alertJobService.queryChain()
                    .select(AlertJob::getAlertId)
                    .eq(AlertJob::getJobId, jobId)
                    .listAs(Long.class);

            val alertList = alertService.queryChain()
                    .in(Alert::getId, alertIds)
                    .list();

            val updateAlertList = new ArrayList<Alert>();
            for (Alert alert : alertList) {
                val name = alert.getName();
                val email = alert.getEmail();
                val sendTime = alert.getSendTime();

                if (sendTime == null || sendTime.isBefore(LocalDateTime.now().minus(silentTime.toMillis(), ChronoUnit.MILLIS))) {
                    alert.setSendTime(LocalDateTime.now());
                    updateAlertList.add(alert);

                    val exception = flinkApiProvider.getJobException(flinkCluster.getJobManagerUrl(), etlJobInstance.getId());
                    this.send(
                            javaMailSender,
                            name,
                            """
                                        <pre>
                                        <strong>%s</strong> 任务发生异常
                                    
                                        %s
                                        </pre>
                                    """.formatted(etlJob.getName(), exception),
                            email
                    );
                }
            }

            alertService.updateBatch(updateAlertList);
        });


    }

    public void testSend(String title, String email) {
        javaMailSender.ifAvailable(javaMailSender -> {
            this.send(javaMailSender, title, "这是一封测试邮件", email);
        });
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
