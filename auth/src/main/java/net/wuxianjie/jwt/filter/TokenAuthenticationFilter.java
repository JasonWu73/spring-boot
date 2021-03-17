package net.wuxianjie.jwt.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import net.wuxianjie.jwt.config.JwtConfig;
import net.wuxianjie.jwt.constant.RedisKeyConstants;
import net.wuxianjie.jwt.model.AuthData;
import net.wuxianjie.jwt.service.AccessTokenServiceImpl;
import net.wuxianjie.jwt.util.JwtUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  public static final String BEARER_PREFIX = "Bearer ";
  public static final String ROLE_PREFIX = "ROLE_";

  private final ObjectMapper objectMapper;

  private final JwtConfig jwtConfig;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {

    String token = getTokenFromRequest(request);
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      AuthData authData = authenticate(token);

      setSpringSecurityContext(authData);
    } catch (AuthenticationException e) {
      // 清除当前线程中的 Spring Security 上下文内容
      SecurityContextHolder.clearContext();

      outputResponse(response, e.getMessage(), HttpStatus.UNAUTHORIZED.value());
      return;
    } catch (Throwable e) {
      log.warn(e.getMessage(), e);

      // 清除当前线程中的 Spring Security 上下文内容
      SecurityContextHolder.clearContext();

      outputResponse(response, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
      return;
    }

    filterChain.doFilter(request, response);
  }

  private String getTokenFromRequest(HttpServletRequest request) throws AuthenticationException {

    String bearerHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (bearerHeader != null && bearerHeader.startsWith(BEARER_PREFIX)) {
      return bearerHeader.substring(BEARER_PREFIX.length());
    }

    return null;
  }

  private AuthData authenticate(String token) throws JsonProcessingException {
    // 解析 Token
    Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);
    String username = (String) claims.get(AccessTokenServiceImpl.TOKEN_USERNAME);

    // 获取 Redis 中的 Access Token
    String tokenInRedis = stringRedisTemplate.opsForValue().get(RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);
    if (tokenInRedis == null) {
      throw new AuthenticationException("Token 已过期");
    }

    AuthData authData = objectMapper.readValue(tokenInRedis, AuthData.class);

    // 比对传入 Token 是否与 Redis 中的 Access Token 一致
    if (!authData.getToken().equals(token)) {
      throw new AuthenticationException("Token 已过期");
    }

    return authData;
  }

  private void setSpringSecurityContext(AuthData authData) {
    // 注意: Spring Security 要求角色为名大写, 且以 `ROLE_` 为前缀
    String rolesHadPrefix = Arrays.stream(authData.getRoles().split(","))
      .reduce("", (s, s2) -> {
        if (Strings.isNotBlank(s)) {
          return s + "," + ROLE_PREFIX + s2.trim().toUpperCase(Locale.ROOT);
        }

        return ROLE_PREFIX + s2.trim().toUpperCase(Locale.ROOT);
      });

    List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(rolesHadPrefix);

    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
      authData, null, authorities));
  }

  private void outputResponse(HttpServletResponse response, String message, int httpStatus) throws IOException {

    ResponseResult<Void> result = ResponseResultWrappers.fail(message);

    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    response.setStatus(httpStatus);
    response.getWriter().write(objectMapper.writeValueAsString(result));
  }
}
