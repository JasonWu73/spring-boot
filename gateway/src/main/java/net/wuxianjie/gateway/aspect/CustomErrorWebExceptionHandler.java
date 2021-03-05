package net.wuxianjie.gateway.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CustomErrorWebExceptionHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    ServerHttpResponse response = exchange.getResponse();

    if (response.isCommitted()) {
      return Mono.error(ex);
    }

    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    if (ex instanceof ResponseStatusException) {
      response.setStatusCode(((ResponseStatusException) ex).getStatus());
    } else {
      response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    log.error("全局异常处理", ex);

    return response
      .writeWith(Mono.fromSupplier(() -> {
        DataBufferFactory bufferFactory = response.bufferFactory();
        try {
          return bufferFactory.wrap(objectMapper.writeValueAsBytes(
            ResponseResultWrappers.error(ex.getMessage())));
        } catch (JsonProcessingException e) {
          log.error("响应时数据异常", ex);
          return bufferFactory.wrap(new byte[0]);
        }
      }));
  }
}
