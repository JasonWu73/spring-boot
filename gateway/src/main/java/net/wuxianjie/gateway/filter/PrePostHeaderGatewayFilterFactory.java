package net.wuxianjie.gateway.filter;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PrePostHeaderGatewayFilterFactory
  extends AbstractGatewayFilterFactory<PrePostHeaderGatewayFilterFactory.Config> {

  public PrePostHeaderGatewayFilterFactory() {
    super(Config.class);
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Arrays.asList("name", "value");
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> chain
      // pre filter
      .filter(exchange.mutate().request(builder ->
        builder.header(config.getName(), config.getValue()).build()
      ).build())
      // post filter
      .then(Mono.fromRunnable(() -> {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
          return;
        }

        response.getHeaders().add("version", "v1.0");
      }));
  }

  @Data
  public static class Config {

    private String name;

    private String value;
  }
}
