package net.wuxianjie.jwt.controller;

import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.config.JwtConfig;
import net.wuxianjie.jwt.util.JwtUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/access_token")
public class AccessTokenController {

  private final JwtConfig jwtConfig;

  @PostMapping
  public Map<String, String> create(String username, String password)
    throws AuthenticationException {

    if (Strings.isBlank(username) || Strings.isBlank(password)) {
      throw new AuthenticationException("用户名或密码为空");
    }

    Map<String, Object> claims = new HashMap<>();

    if (username.equals("wxj") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "wxj");
    } else if (username.equals("jason") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "jason");
    } else if (username.equals("bruce") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "bruce");
    } else {
      throw new AuthenticationException("用户名或密码错误");
    }

    String token = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims, 20);

    return new HashMap<>() {{
      put("token", token);
    }};
  }

  @GetMapping("/verify")
  public Map<String, Object> verify(@NotBlank(message = "Token 不能为空") String token) {
    // 解析 token
    Claims claims = JwtUtils.parseToken(jwtConfig.getSecretKey(), token);

    // 获取用户名
    String username = (String) claims.get(CommonConstants.TOKEN_USERNAME);
    if (Strings.isBlank(username)) {
      throw new AuthenticationException("Token 主体不存在 " + CommonConstants.TOKEN_USERNAME);
    }

    return new HashMap<>() {{
      put("valid", true);
      put("username", username);
    }};
  }
}
