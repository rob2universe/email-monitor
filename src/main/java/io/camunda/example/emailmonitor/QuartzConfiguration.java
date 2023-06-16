package io.camunda.example.emailmonitor;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
    import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfiguration {

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob().ofType(EmailPollerJob.class)
          .storeDurably()
          .withIdentity("Qrtz_EmailPoller_Detail")  
          .withDescription("Invoke Email Job service...")
          .build();
    }

    @Bean
    public Trigger emailPollerJobTrigger(JobDetail emailPollerJobDetail) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInSeconds(60) // Poll every 60 seconds
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(emailPollerJobDetail)
                .withIdentity("Qrtz_EmailPollerTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }

    @Bean
    public SchedulerFactoryBean scheduler(Trigger trigger, JobDetail job) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        // schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));

        //schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setJobDetails(job);
        schedulerFactory.setTriggers(trigger);
        //schedulerFactory.setDataSource(quartzDataSource);
        return schedulerFactory;
    }

}
