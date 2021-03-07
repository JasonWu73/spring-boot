package net.wuxianjie.jwt.service;

import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.config.JwtConfig;
import net.wuxianjie.jwt.constant.RedisKeyConstants;
import net.wuxianjie.jwt.model.Token;
import net.wuxianjie.jwt.model.TokenData;
import net.wuxianjie.jwt.util.JwtUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

  public static final String TOKEN_USERNAME = "user";

  public static final String TOKEN_TYPE = "type";

  public static final String TOKEN_TYPE_REFRESH_VALUE = "refresh";

  public static final int EXPIRED_ON_MINUTES = 1;

  private final JwtConfig jwtConfig;

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public Token getOrCreateToken(String username, String password)
    throws AuthenticationException {

    Map<String, Object> claims = new HashMap<>();

    if ((username.equals("wxj") && password.equals("123")) ||
      (username.equals("jason") && password.equals("123")) ||
      (username.equals("bruce") && password.equals("123"))) {

      Token tokenResult = getTokenFromRedis(username);

      if (tokenResult != null && tokenResult.getToken() != null) {
        return tokenResult;
      }

      claims.put(TOKEN_USERNAME, username);
    } else {
      throw new AuthenticationException("用户名或密码错误");
    }

    String token = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    claims.put(TOKEN_TYPE, TOKEN_TYPE_REFRESH_VALUE);
    String refreshToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    setTokenToRedis(username, token, refreshToken);

    return new Token(EXPIRED_ON_MINUTES * 60L, token, refreshToken);
  }

  @Override
  public TokenData verifyToken(String token) throws AuthenticationException {
    // 解析 Token
    TokenData tokenData = parseToken(token);

    // 判断该 Token 是否存在于 Redis
    Token tokenRedis = getTokenFromRedis(tokenData.getUsername());

    if (tokenRedis == null) {
      throw new AuthenticationException("Token 已失效");
    }

    if (tokenRedis.getToken() != null && tokenRedis.getToken().equals(token)) {
      return tokenData;
    }

    if (tokenRedis.getRefreshToken() != null && tokenRedis.getRefreshToken().equals(token)) {
      return tokenData;
    }

    throw new AuthenticationException("Token 已失效");
  }

  @Override
  public Token refreshToken(String token) throws AuthenticationException {
    // 解析 Token
    TokenData tokenData = parseToken(token);

    // 判断 Token 是否为用于刷新的 Token
    if (tokenData.getType() == null ||
      !tokenData.getType().equals(TOKEN_TYPE_REFRESH_VALUE)) {
      throw new AuthenticationException("不是用于刷新的 Token");
    }

    // 判断 Token 是否存在于 Redis 中, 且与用于刷新的 Token 是否一致
    Token tokenRedis = getTokenFromRedis(tokenData.getUsername());

    if (tokenRedis == null ||
      tokenRedis.getRefreshToken() == null ||
      !tokenRedis.getRefreshToken().equals(token)) {
      throw new AuthenticationException("Token 已失效");
    }

    // 生成用于访问和刷新的 Token
    Map<String, Object> claims = new HashMap<>();

    claims.put(TOKEN_USERNAME, tokenData.getUsername());

    String newToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    claims.put(TOKEN_TYPE, TOKEN_TYPE_REFRESH_VALUE);
    String refreshToken = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    // 将生成的 Token 加入 Redis 中
    setTokenToRedis(tokenData.getUsername(), newToken, refreshToken);

    return new Token(EXPIRED_ON_MINUTES * 60L, newToken, refreshToken);
  }

  private Token getTokenFromRedis(String username) {

    String tokenKey = getTokenKeyInRedis(username);
    String refreshTokenKey = getRefreshTokenKeyInRedis(username);

    ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();

    String token = operations.get(tokenKey);
    String refreshToken = operations.get(refreshTokenKey);

    if (token != null) {
      return new Token(stringRedisTemplate.getExpire(tokenKey), token,
        refreshToken);
    }

    if (refreshToken != null) {
      return new Token(null, null, refreshToken);
    }

    return null;
  }

  private void setTokenToRedis(String username, String token, String refreshToken) {

    String tokenKey = getTokenKeyInRedis(username);
    String refreshTokenKey = getRefreshTokenKeyInRedis(username);

    ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();

    operations.set(tokenKey, token, EXPIRED_ON_MINUTES, TimeUnit.MINUTES);
    operations.set(refreshTokenKey, refreshToken, EXPIRED_ON_MINUTES * 2, TimeUnit.MINUTES);
  }

  private String getTokenKeyInRedis(String username) {
    return RedisKeyConstants.REDIS_KEY_TOKEN_PREFIX + username;
  }

  private String getRefreshTokenKeyInRedis(String username) {
    return RedisKeyConstants.REDIS_KEY_REFRESH_TOKEN_PREFIX + username;
  }

  private TokenData parseToken(String token) throws AuthenticationException {
    // 解析 token
    Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);

    // 获取用户名
    String username = (String) claims.get(TOKEN_USERNAME);

    // 获取类型
    String type = (String) claims.get(TOKEN_TYPE);

    if (Strings.isBlank(username)) {
      throw new AuthenticationException("Token 主体不存在 " + TOKEN_USERNAME);
    }

    return new TokenData(username, type, claims.getNotBefore());
  }
}
