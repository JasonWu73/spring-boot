package net.wuxianjie.jwt.service;

import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.common.exception.BadRequestException;
import net.wuxianjie.jwt.config.JwtConfig;
import net.wuxianjie.jwt.constant.RedisKeyConstants;
import net.wuxianjie.jwt.model.Token;
import net.wuxianjie.jwt.model.TokenData;
import net.wuxianjie.jwt.util.JwtUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

  public static final String TOKEN_USERNAME = "user";

  public static final String TOKEN_TYPE = "type";

  public static final String TOKEN_TYPE_REFRESH_VALUE = "refresh";

  public static final int TOKEN_EXPIRED_ON_MINUTES = 1;

  private final JwtConfig jwtConfig;

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public Token getOrCreateToken(String username, String password)
    throws AuthenticationException {

    // 判断用户名密码是否正确
    boolean validUser = isValidUser(username, password);
    if (!validUser) {
      throw new AuthenticationException("用户名或密码错误");
    }

    // 从 Redis 中查询 Access Token
    String accessToken = getTokenFromRedis(
      RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);

    if (accessToken != null) {
      // 从 Redis 中查询 Access Token 有效期
      long expireSeconds = ttlTokenFromRedis(
        RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);

      // 从 Redis 中查询 Refresh Token
      String refreshToken = getTokenFromRedis(
        RedisKeyConstants.REFRESH_TOKEN_PREFIX + username);

      return new Token(expireSeconds, accessToken, refreshToken);
    }

    // 生成 Access Token 与 Refresh Token
    return generateToken(username);
  }

  @Override
  public Token refreshToken(String token)
    throws AuthenticationException, BadRequestException {
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
    String refreshToken = getTokenFromRedis(
      RedisKeyConstants.REFRESH_TOKEN_PREFIX + username);

    // 比较传入的 Token 与 Redis 中的 Token
    if (!refreshToken.equals(token)) {
      throw new AuthenticationException("Token 不匹配");
    }

    // Redis 中是否已存在 Access Token
    boolean existedAccessToken = isExistedTokenInRedis(
      RedisKeyConstants.ACCESS_TOKEN_PREFIX + username);

    if (existedAccessToken) {
      throw new BadRequestException("Token 不允许刷新");
    }

    // 生成 Access Token 与 Refresh Token
    return generateToken(username);
  }

  @Override
  public TokenData verifyToken(String token) throws AuthenticationException {
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
    String tokenInRedis = getTokenFromRedis(keyPrefix + username);

    // 比较传入的 Token 与 Redis 中的 Token
    if (tokenInRedis == null || !tokenInRedis.equals(token)) {
      throw new AuthenticationException("Token 已过期");
    }

    // 返回解析结果
    return new TokenData(username, type, notBefore);
  }

  private boolean isValidUser(String username, String password) {
    return (username.equals("wxj") && password.equals("123")) ||
      (username.equals("jason") && password.equals("123")) ||
      (username.equals("bruce") && password.equals("123"));
  }

  private String getTokenFromRedis(String key) {
    return stringRedisTemplate.opsForValue().get(key);
  }

  private Long ttlTokenFromRedis(String key) {
    return stringRedisTemplate.getExpire(key);
  }

  private void saveTokenToRedis(String username, String accessToken, String refreshToken) {

    stringRedisTemplate.opsForValue().set(
      RedisKeyConstants.ACCESS_TOKEN_PREFIX + username,
      accessToken, TOKEN_EXPIRED_ON_MINUTES, TimeUnit.MINUTES);

    // Refresh Token 是 Access Token 的两倍有效期
    stringRedisTemplate.opsForValue().set(
      RedisKeyConstants.REFRESH_TOKEN_PREFIX + username,
      refreshToken, TOKEN_EXPIRED_ON_MINUTES * 2, TimeUnit.MINUTES);
  }

  private Token generateToken(String username) {
    // 构造 JWT 主体声明
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_USERNAME, username);

    // 生成 Access Token
    String accessToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    // 生成 Refresh Token
    claims.put(TOKEN_TYPE, TOKEN_TYPE_REFRESH_VALUE);
    String refreshToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    // 存入 Redis
    saveTokenToRedis(username, accessToken, refreshToken);

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
