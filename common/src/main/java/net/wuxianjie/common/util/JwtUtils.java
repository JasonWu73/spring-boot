package net.wuxianjie.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import net.wuxianjie.common.exception.UserAccessDeniedException;

public class JwtUtils {

  public static String generateToken(final String secretKey,
    final Map<String, Object> claims, final int expMinutes) {
    return Jwts.builder()
      .setClaims(claims)
      .setNotBefore(new Date())
      .setExpiration(Date.from(
        LocalDateTime.now().plusMinutes(expMinutes)
          .atZone(ZoneId.systemDefault()).toInstant()))
      .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
      .compact();
  }

  public static Claims parseToken(final String secretKey, final String token)
    throws UserAccessDeniedException {
    final Jws<Claims> jws;
    try {
      jws = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
        .build()
        .parseClaimsJws(token);

      return jws.getBody();
    } catch (MalformedJwtException e) {
      throw new UserAccessDeniedException("Token 格式错误: " + e.getMessage());
    } catch (SignatureException e) {
      throw new UserAccessDeniedException("Token 签名错误: " + e.getMessage());
    } catch (ExpiredJwtException e) {
      throw new UserAccessDeniedException("Token 已过期: " + e.getMessage());
    }
  }
}
