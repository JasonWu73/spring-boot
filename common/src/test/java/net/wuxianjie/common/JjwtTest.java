package net.wuxianjie.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JjwtTest {

  public static final String SECRET_KEY = "6/ATA2JKtDzT0jhs+loVxzaGiwROIn4bThvdhAIn5wo=";
  public static final String JWT = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoid3V4aWFuamllIn0.hUKPFLX8uhkC1e39_Qv-wYNVGkbo7kYzosjlbRjgeRY";

  @Test
  public void createSecretKey() {

    final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    final String secretString = Encoders.BASE64.encode(key.getEncoded());
    log.info("JWT 密钥: {}", secretString);
  }

  @Test
  public void createJws() {

    final Map<String, Object> claims = new HashMap<>() {{
      put("user", "wuxianjie");
    }};

    final String jws = Jwts.builder()
      .setNotBefore(new Date())
      .setExpiration(Date.from(LocalDateTime.now().plusMinutes(20).atZone(ZoneId.systemDefault()).toInstant()))
      .setClaims(claims)
      .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(JjwtTest.SECRET_KEY)))
      .compact();
    log.info("生成 JWT: {}", jws);
  }

  @Test
  public void readJws() {

    final Jws<Claims> jws;

    try {
      jws = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(JjwtTest.SECRET_KEY)))
        .build()
        .parseClaimsJws(JjwtTest.JWT);

      final Claims body = jws.getBody();
      log.info("读取 JWT 主体信息 [user: {}]", body.get("user"));
    } catch (JwtException e) {
      log.warn("JWT 验证失败: {}", e.getMessage());
    }
  }
}
