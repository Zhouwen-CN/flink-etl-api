package com.etl.api.config;

import com.etl.api.service.manager.ScheduleJobManager;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.val;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

/**
 * quartz job监听器，暴露执行次数和耗时指标
 */
@Component
public class JobListenerMeterBinder implements JobListener, MeterBinder {
    private MeterRegistry registry;

    @Override
    public String getName() {
        return "quartz.job";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        Timer.Sample sample = Timer.start(registry);
        context.put(Timer.Sample.class, sample);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // do noting
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        Timer.Sample sample = (Timer.Sample) context.get(Timer.Sample.class);
        sample.stop(Timer.builder(this.getName())
                .tags(this.getTags(context))
                .register(registry)
        );
    }

    private String[] getTags(JobExecutionContext context) {
        val key = context.getJobDetail().getKey();
        val mergedJobDataMap = context.getMergedJobDataMap();
        val scheduleName = mergedJobDataMap.getString(ScheduleJobManager.SCHEDULE_NAME);
        return new String[]{
                "group",
                key.getGroup(),
                "job",
                key.getName(),
                "name",
                scheduleName
        };
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
    }
}
