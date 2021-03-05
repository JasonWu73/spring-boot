package net.wuxianjie.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomCircuitBreakerConfig {

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
      .circuitBreakerConfig(CircuitBreakerConfig.custom()
        .waitDurationInOpenState(Duration.ofSeconds(60)) // 熔断器开启后多少时间后去尝试请求服务
        .minimumNumberOfCalls(20) // 至少有 n 个请求才进行 `failureRateThreshold` 错误百分比计算
        .failureRateThreshold(0.5f) // 失败率
        .build()
      )
      .timeLimiterConfig(TimeLimiterConfig.custom()
        .timeoutDuration(Duration.ofSeconds(5)) // 超时时间
        .build()).build());
  }
}
