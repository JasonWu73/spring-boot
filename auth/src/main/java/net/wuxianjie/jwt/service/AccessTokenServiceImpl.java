package net.wuxianjie.jwt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.common.exception.BadRequestException;
import net.wuxianjie.jwt.config.JwtConfig;
import net.wuxianjie.jwt.constant.RedisKeyConstants;
import net.wuxianjie.jwt.model.AuthData;
import net.wuxianjie.jwt.model.Token;
import net.wuxianjie.jwt.model.TokenData;
import net.wuxianjie.jwt.model.User;
import net.wuxianjie.jwt.util.JwtUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

  public static final String TOKEN_USERNAME = "user";
  public static final String TOKEN_TYPE = "type";
  public static final String TOKEN_TYPE_REFRESH_VALUE = "refresh";
  public static final int TOKEN_EXPIRED_ON_MINUTES = 1;

  /**
   * 编码后的编码: 123
   */
  public static final String ENCODED_PASSWORD = "$2a$10$9Tq5H9wCOiRg97zR5K.6ye.5TIAQUVhDPFhm5YsbvSgpJhwTj3.yW";

  private final JwtConfig jwtConfig;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public Token getOrCreateToken(String username, String password)
    throws AuthenticationException, JsonProcessingException {

    // 判断用户名密码是否正确
    User validUser = getValidUser(username, password);
    if (validUser == null) {
      throw new AuthenticationException("用户名或密码错误");
    }

    // 从 Redis 中查询 Access Token
    AuthData accessAuthData = getTokenFromRedis(
      RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);

    if (accessAuthData != null) {
      // 从 Redis 中查询 Access Token 有效期
      long expireSeconds = ttlTokenFromRedis(
        RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);

      // 从 Redis 中查询 Refresh Token
      AuthData refreshAuthData = getTokenFromRedis(
        RedisKeyConstants.REFRESH_TOKEN_PREFIX + username);

      return new Token(expireSeconds, accessAuthData.getToken(), Objects.requireNonNull(refreshAuthData).getToken());
    }

    // 构造写入 Redis 中的用户数据
    AuthData authData = new AuthData();
    authData.setUserId(validUser.getUserId());
    authData.setUsername(validUser.getUsername());
    authData.setRoles(validUser.getRoles());

    // 生成 Access Token 与 Refresh Token
    return generateToken(authData);
  }

  @Override
  public Token refreshToken(String token)
    throws AuthenticationException, BadRequestException, JsonProcessingException {
    // 解析 Token
    Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);
    String username = (String) claims.get(TOKEN_USERNAME);
    String type = (String) claims.get(TOKEN_TYPE);

    // 获取 Token 类型
    boolean accessTokenType = isAccessToken(type);

    if (accessTokenType) {
      throw new AuthenticationException("Token 类型错误");
    }

    // 从 Redis 中查询 Refresh Token
    AuthData authData = getTokenFromRedis(
      RedisKeyConstants.REFRESH_TOKEN_PREFIX + username);

    // 比较传入的 Token 与 Redis 中的 Token
    if (authData == null || !authData.getToken().equals(token)) {
      throw new AuthenticationException("Token 已过期");
    }

    // Redis 中是否已存在 Access Token
    boolean existedAccessToken = isExistedTokenInRedis(
      RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);

    if (existedAccessToken) {
      throw new BadRequestException("Token 不允许刷新");
    }

    // 生成 Access Token 与 Refresh Token
    return generateToken(authData);
  }

  @Override
  public TokenData verifyToken(String token) throws AuthenticationException, JsonProcessingException {
    // 解析 Token
    Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);
    String username = (String) claims.get(TOKEN_USERNAME);
    String type = (String) claims.get(TOKEN_TYPE);
    Date notBefore = claims.getNotBefore();

    // 判断 Token 类型
    boolean accessTokenType = isAccessToken(type);

    String keyPrefix = RedisKeyConstants.REFRESH_TOKEN_PREFIX;
    if (accessTokenType) {
      keyPrefix = RedisKeyConstants.ACCESS_TOKEN_PREFIX;
    }

    // 从 Redis 中查询 Token
    AuthData authData = getTokenFromRedis(keyPrefix + username);

    // 比较传入的 Token 与 Redis 中的 Token
    if (authData == null || !authData.getToken().equals(token)) {
      throw new AuthenticationException("Token 已过期");
    }

    // 返回解析结果
    return new TokenData(username, type, notBefore);
  }

  private User getValidUser(String username, String password) {

    User guest = new User(1, "guest", ENCODED_PASSWORD, "");
    User user = new User(2, "user", ENCODED_PASSWORD, "user");
    User admin = new User(3, "admin", ENCODED_PASSWORD, "user,admin");

    if (guest.getUsername().equals(username) && passwordEncoder.matches(password, guest.getPassword())) {
      return guest;
    }

    if (user.getUsername().equals(username) && passwordEncoder.matches(password, user.getPassword())) {
      return user;
    }

    if (admin.getUsername().equals(username) && passwordEncoder.matches(password, admin.getPassword())) {
      return admin;
    }

    return null;
  }

  private AuthData getTokenFromRedis(String key) throws JsonProcessingException {

    String authStr = stringRedisTemplate.opsForValue().get(key);
    if (authStr == null) {
      return null;
    }

    return objectMapper.readValue(authStr, AuthData.class);
  }

  private Long ttlTokenFromRedis(String key) {
    return stringRedisTemplate.getExpire(key);
  }

  private void saveTokenToRedis(AuthData authData, String accessToken, String refreshToken) throws JsonProcessingException {
    // 存入 Access Token
    authData.setToken(accessToken);
    stringRedisTemplate.opsForValue().set(
      RedisKeyConstants.ACCESS_TOKEN_PREFIX + authData.getUsername(),
      objectMapper.writeValueAsString(authData), TOKEN_EXPIRED_ON_MINUTES, TimeUnit.MINUTES);

    // Refresh Token 是 Access Token 的两倍有效期
    authData.setToken(refreshToken);
    stringRedisTemplate.opsForValue().set(
      RedisKeyConstants.REFRESH_TOKEN_PREFIX + authData.getUsername(),
      objectMapper.writeValueAsString(authData), TOKEN_EXPIRED_ON_MINUTES * 2, TimeUnit.MINUTES);
  }

  private Token generateToken(AuthData authData) throws JsonProcessingException {
    // 构造 JWT 主体声明
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_USERNAME, authData.getUsername());

    // 生成 Access Token
    String accessToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    // 生成 Refresh Token
    claims.put(TOKEN_TYPE, TOKEN_TYPE_REFRESH_VALUE);
    String refreshToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    // 存入 Redis
    saveTokenToRedis(authData, accessToken, refreshToken);

    return new Token(TOKEN_EXPIRED_ON_MINUTES * 60L, accessToken, refreshToken);
  }

  /**
   * 不是 Access Token 就是 Refresh Token
   */
  private boolean isAccessToken(String type) {
    return type == null || !type.equals(TOKEN_TYPE_REFRESH_VALUE);
  }

  private boolean isExistedTokenInRedis(String key) {
    return stringRedisTemplate.opsForValue().get(key) != null;
  }
}
