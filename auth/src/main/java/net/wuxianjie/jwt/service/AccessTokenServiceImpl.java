package net.wuxianjie.jwt.service;

import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.config.JwtConfig;
import net.wuxianjie.jwt.constant.RedisKeyConstants;
import net.wuxianjie.jwt.model.CreateToken;
import net.wuxianjie.jwt.model.ParseToken;
import net.wuxianjie.jwt.util.JwtUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {

  public static final int EXPIRED_ON_MINUTES = 1;

  private final JwtConfig jwtConfig;

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public CreateToken getOrCreateToken(String username, String password)
    throws AuthenticationException {

    Map<String, Object> claims = new HashMap<>();

    if ((username.equals("wxj") && password.equals("123")) ||
      (username.equals("jason") && password.equals("123")) ||
      (username.equals("bruce") && password.equals("123"))) {

      CreateToken tokenResult = getTokenFromRedis(username);

      if (tokenResult != null) {
        return tokenResult;
      }

      claims.put(CommonConstants.TOKEN_USERNAME, username);
    } else {
      throw new AuthenticationException("用户名或密码错误");
    }

    String token = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims, EXPIRED_ON_MINUTES);

    setTokenToRedis(username, token);

    return new CreateToken(EXPIRED_ON_MINUTES * 60L, token);
  }

  @Override
  public ParseToken verifyToken(String token) throws AuthenticationException {
    // 解析 Token
    ParseToken tokenData = parseToken(token);

    // 判断该 Token 是否存在于 Redis
    CreateToken tokenRedis = getTokenFromRedis(tokenData.getUsername());

    if (tokenRedis == null) {
      throw new AuthenticationException("Token 已失效");
    }

    return tokenData;
  }

  private CreateToken getTokenFromRedis(String username) {

    String tokenKey = getTokenKeyInRedis(username);

    String token = stringRedisTemplate.opsForValue().get(tokenKey);

    if (token != null) {
      return new CreateToken(stringRedisTemplate.getExpire(tokenKey), token);
    }

    return null;
  }

  private void setTokenToRedis(String username, String token) {

    String tokenKey = getTokenKeyInRedis(username);

    stringRedisTemplate.opsForValue().set(tokenKey, token, EXPIRED_ON_MINUTES, TimeUnit.MINUTES);
  }

  private String getTokenKeyInRedis(String username) {
    return RedisKeyConstants.REDIS_KET_TOKEN_PREFIX + username;
  }

  private ParseToken parseToken(String token) throws AuthenticationException {
    // 解析 token
    Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);

    // 获取用户名
    String username = (String) claims.get(CommonConstants.TOKEN_USERNAME);

    if (Strings.isBlank(username)) {
      throw new AuthenticationException("Token 主体不存在 " + CommonConstants.TOKEN_USERNAME);
    }

    return new ParseToken(username, claims.getNotBefore(), claims.getExpiration());
  }
}
