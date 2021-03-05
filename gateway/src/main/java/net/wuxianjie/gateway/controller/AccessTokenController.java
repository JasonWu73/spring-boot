package net.wuxianjie.gateway.controller;

import java.util.HashMap;
import java.util.Map;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.util.JwtUtils;
import net.wuxianjie.gateway.config.JwtConfig;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessTokenController {

  private final JwtConfig jwtConfig;

  public AccessTokenController(JwtConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  @PostMapping("/access_token")
  public Map<String, String> createAccessToken(
    @RequestParam(value = "username", required = false) final String username,
    @RequestParam(value = "password", required = false) final String password) {
    if (Strings.isBlank(username) || Strings.isBlank(password)) {
      return new HashMap<>() {{
        put("status", "error");
        put("error", "用户名或密码为空");
      }};
    }

    Map<String, Object> claims = new HashMap<>();

    if (username.equals("wxj") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "wxj");
    } else if (username.equals("jason") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "jason");
    } else {
      return new HashMap<>() {{
        put("status", "error");
        put("error", "用户名或密码错误");
      }};
    }

    final String token = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims);

    return new HashMap<>() {{
      put("status", "success");
      put("token", token);
    }};
  }
}
