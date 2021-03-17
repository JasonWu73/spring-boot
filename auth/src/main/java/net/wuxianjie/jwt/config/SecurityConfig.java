package net.wuxianjie.jwt.config;

import lombok.RequiredArgsConstructor;
import net.wuxianjie.jwt.filter.TokenAuthenticationFilter;
import net.wuxianjie.jwt.security.TokenAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint;
  private final TokenAuthenticationFilter tokenAuthenticationFilter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      // 授权
      .authorizeRequests()
      // 按顺序比对
      .antMatchers("/res/public").permitAll() // principal 为 "anonymous"
      .antMatchers("/access_token/**").permitAll()
      .anyRequest().authenticated()
      .and()
      // 异常
      .exceptionHandling()
      .authenticationEntryPoint(tokenAuthenticationEntryPoint) // 401
      .and()
      // 禁用 CSRF 措施
      .csrf()
      .disable()
      // 无状态授权机制，每次请求都需要验证 Token，
      // 故不需要设置服务器端 `HttpSession`，
      // 也不需要设置客户端 `JSESSIONID` Cookies
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      // 解决浏览器跨域检查请求 OPTIONS 时返回 401 的问题
      .cors()
      .configurationSource(httpServletRequest ->
        new CorsConfiguration().applyPermitDefaultValues())
      .and()
      // 添加 JWT 身份验证处理过滤器
      .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
