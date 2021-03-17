package net.wuxianjie.web.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseBodyAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper objectMapper;

  @Override
  public boolean supports(MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request, ServerHttpResponse response) {

    // 解决 Controller 方法返回 `String` 时转换异常的问题
    if (body instanceof String) {
      try {
        return objectMapper.writeValueAsString(ResponseResultWrappers.success(body));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    if (body instanceof ResponseResult || body instanceof ResponseEntity) {
      return body;
    }

    return ResponseResultWrappers.success(body);
  }
}
