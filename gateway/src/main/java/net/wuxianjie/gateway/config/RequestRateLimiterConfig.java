package net.wuxianjie.gateway.config;

import net.wuxianjie.common.exception.HttpStatusException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Configuration
public class RequestRateLimiterConfig {

  @Bean
  KeyResolver userKeyResolver() {
    return exchange -> {
      String tokenBearer = exchange.getRequest().getHeaders().getFirst("Authorization");
      if (Strings.isBlank(tokenBearer)) {
        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "缺少验证头字段");
      }
      return Mono.just(tokenBearer);
    };
  }
}
