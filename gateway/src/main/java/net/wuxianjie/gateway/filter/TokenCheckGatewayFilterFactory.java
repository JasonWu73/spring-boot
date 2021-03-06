package net.wuxianjie.gateway.filter;

import io.jsonwebtoken.Claims;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.UserAccessDeniedException;
import net.wuxianjie.common.util.JwtUtils;
import net.wuxianjie.gateway.config.JwtConfig;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCheckGatewayFilterFactory
  extends AbstractGatewayFilterFactory<Object> {

  private final JwtConfig jwtConfig;

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> {
      try {
        // 获取 token
        String token = getToken(exchange);

        // 解析 token
        Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);

        // 获取用户名
        String username = (String) claims.get(CommonConstants.TOKEN_USERNAME);
        if (Strings.isBlank(username)) {
          throw new UserAccessDeniedException("Token 主体不存在 " + CommonConstants.TOKEN_USERNAME);
        }

        // 将用户名写入请求头中
        return chain.filter(exchange.mutate().request(builder ->
          builder.header(CommonConstants.REQUEST_HEADER_USERNAME, username)).build());
      } catch (UserAccessDeniedException e) {
        log.warn(e.getMessage());

        URI uri = URI.create("http://localhost/" + CommonConstants.PATH_UNAUTHORIZED);
        ServerHttpRequest request = exchange.getRequest().mutate()
          .path(uri.getRawPath()).build();

        Route currentRoute = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        Route newRoute = Route.async()
          .id(Objects.requireNonNull(currentRoute).getId())
          .asyncPredicate(currentRoute.getPredicate())
          .uri(uri)
          .build();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, newRoute);

        return chain.filter(exchange.mutate().request(request).build());
      }
    };
  }

  private String getToken(ServerWebExchange exchange) throws UserAccessDeniedException {
    String bearerHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (Strings.isBlank(bearerHeader)) {
      throw new UserAccessDeniedException(String.format("请求头 %s 不存在", HttpHeaders.AUTHORIZATION));
    }

    String[] parts = bearerHeader.split(" ");
    if (parts.length != 2) {
      throw new UserAccessDeniedException(String.format("请求头 %s 格式错误: 分段长度不为2", HttpHeaders.AUTHORIZATION));
    }

    String scheme = parts[0];
    String token = parts[1];

    if (!"Bearer".equals(scheme)) {
      throw new UserAccessDeniedException(String.format("请求头 %s 格式错误: Bearer 不匹配", HttpHeaders.AUTHORIZATION));
    }

    return token;
  }
}
