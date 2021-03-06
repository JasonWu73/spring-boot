package net.wuxianjie.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.common.exception.HttpStatusException;
import net.wuxianjie.common.model.ResponseResult;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCheckGatewayFilterFactory
  extends AbstractGatewayFilterFactory<Object> {

  private final ObjectMapper objectMapper;

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> {
      try {
        // 获取 token
        String token = getToken(exchange);

        // 请求认证服务, 验证 Token 是否合法并获取解析后的身份数据
        String username = authenticate(token);

        // 将用户名写入请求头中
        return chain.filter(exchange.mutate().request(builder ->
          builder.header(CommonConstants.REQUEST_HEADER_USERNAME, username)).build());

      } catch (AuthenticationException e) {
        log.warn(e.getMessage());

        // 对于连 Authorization 请求头都写错的, 跳转到其他 URL
        return redirectUrl(exchange, chain);
      }
    };
  }

  private String getToken(ServerWebExchange exchange) throws AuthenticationException {

    String bearerHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    if (Strings.isBlank(bearerHeader)) {
      throw new AuthenticationException(String.format("请求头 %s 不存在", HttpHeaders.AUTHORIZATION));
    }

    String[] parts = bearerHeader.split(" ");
    if (parts.length != 2) {
      throw new AuthenticationException(String.format("请求头 %s 格式错误: 分段长度不为2", HttpHeaders.AUTHORIZATION));
    }

    String scheme = parts[0];
    String token = parts[1];

    if (!"Bearer".equals(scheme)) {
      throw new AuthenticationException(String.format("请求头 %s 格式错误: Bearer 不匹配", HttpHeaders.AUTHORIZATION));
    }

    return token;
  }

  private String authenticate(String token) {

    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<JsonNode> authResponse;

    try {
      authResponse = restTemplate
        .getForEntity(CommonConstants.URL_TOKEN_VERIFY + token, JsonNode.class);

    } catch (IllegalArgumentException e) {
      throw new HttpStatusException(HttpStatus.BAD_REQUEST, e.getMessage());

    } catch (HttpClientErrorException e) {
      try {
        TypeReference<ResponseResult<Void>> ref = new TypeReference<>() {
        };
        ResponseResult<Void> errorResponse = objectMapper.readValue(e.getResponseBodyAsString(), ref);

        throw new HttpStatusException(e.getStatusCode(), errorResponse.getError());

      } catch (JsonProcessingException e1) {
        throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e1.getMessage());
      }
    }

    JsonNode authBody = Objects.requireNonNull(authResponse.getBody());

    if (!authBody.get("status").asText().equals("success")) {
      throw new HttpStatusException(authResponse.getStatusCode(), authBody.get("error").asText());
    }

    JsonNode authData = authBody.get("data");

    return authData.get("username").asText();
  }

  private Mono<Void> redirectUrl(ServerWebExchange exchange, GatewayFilterChain chain) {
    // 修改请求路径
    URI uri = URI.create(CommonConstants.DOMAIN_NAME + CommonConstants.PATH_UNAUTHENTICATED);
    ServerHttpRequest request = exchange.getRequest().mutate()
      .path(uri.getRawPath()).build();

    // 修改路由
    Route currentRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    Route newRoute = Route.async()
      .id(Objects.requireNonNull(currentRoute).getId())
      .asyncPredicate(currentRoute.getPredicate())
      .uri(uri)
      .build();

    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);

    // 写入修改后的请求
    return chain.filter(exchange.mutate().request(request).build());
  }
}
