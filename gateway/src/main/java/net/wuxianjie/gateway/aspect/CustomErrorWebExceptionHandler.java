package net.wuxianjie.gateway.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.exception.HttpStatusException;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.annotation.Order;
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
@Order(NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1)
@RequiredArgsConstructor
public class CustomErrorWebExceptionHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    if (ex instanceof ResponseStatusException) {
      httpStatus = ((ResponseStatusException) ex).getStatus();
    }

    if (ex instanceof HttpStatusException) {
      httpStatus = ((HttpStatusException) ex).getHttpStatus();
    }

    if (httpStatus.is4xxClientError()) {
      log.warn("客户端错误: {} - {}", exchange.getRequest().getPath(), ex.getMessage());
    } else {
      log.error("全局异常处理", ex);
    }

    ServerHttpResponse response = exchange.getResponse();

    if (response.isCommitted()) {
      return Mono.error(ex);
    }

    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    response.setStatusCode(httpStatus);

    return response
      .writeWith(Mono.fromSupplier(() -> {
        DataBufferFactory bufferFactory = response.bufferFactory();
        try {
          return bufferFactory.wrap(objectMapper.writeValueAsBytes(
            ResponseResultWrappers.fail(
              ex.getMessage() != null ? ex.getMessage() : "Null Pointer Exception")));
        } catch (JsonProcessingException e) {
          log.error("响应时数据异常", ex);
          return bufferFactory.wrap(new byte[0]);
        }
      }));
  }
}
