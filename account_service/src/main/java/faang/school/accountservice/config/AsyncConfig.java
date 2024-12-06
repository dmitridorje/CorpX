package faang.school.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "calculatePercentsExecutor")
    public Executor calculatePercentsExecutor() {
        int waitTime = 200;
        int executionTime = 100;
        int systemCores = Runtime.getRuntime().availableProcessors();
        int corePoolSize = systemCores * (1 + waitTime / executionTime);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("CalcPercentAsyncThread-");
        executor.initialize();
        return executor;
    }
}
