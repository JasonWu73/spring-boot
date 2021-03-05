package net.wuxianjie.gateway.config;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Getter
  @Value("${my.jwtKey}")
  private String secretKey;

  @Bean
  public JwtParser jwtParser() {
    return Jwts.parserBuilder().setSigningKey(secretKey).build();
  }
}
