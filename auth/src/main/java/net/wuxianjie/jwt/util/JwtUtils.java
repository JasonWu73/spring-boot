package net.wuxianjie.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import java.util.Map;
import net.wuxianjie.common.exception.AuthenticationException;

public class JwtUtils {

  public static String generateToken(String secretKey, Map<String, Object> claims) {
    return Jwts.builder()
      .setClaims(claims)
      .setNotBefore(new Date())
      .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
      .compact();
  }

  public static Claims parseToken(String secretKey, String token)
    throws AuthenticationException {

    Jws<Claims> jws;

    try {
      jws = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
        .build()
        .parseClaimsJws(token);

      return jws.getBody();

    } catch (MalformedJwtException e) {
      throw new AuthenticationException("Token 格式错误: " + e.getMessage(), e);
    } catch (SignatureException e) {
      throw new AuthenticationException("Token 签名错误: " + e.getMessage(), e);
    } catch (ExpiredJwtException e) {
      throw new AuthenticationException("Token 已过期: " + e.getMessage(), e);
    }
  }
}
