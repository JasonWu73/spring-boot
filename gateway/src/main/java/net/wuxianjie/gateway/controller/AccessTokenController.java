package net.wuxianjie.gateway.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.UserAccessDeniedException;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.JwtUtils;
import net.wuxianjie.common.util.ResponseResultWrappers;
import net.wuxianjie.gateway.config.JwtConfig;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/access_token")
public class AccessTokenController {

  private final JwtConfig jwtConfig;

  @PostMapping
  public ResponseResult<Map<String, String>> createAccessToken(
    @RequestParam(value = "username", required = false) String username,
    @RequestParam(value = "password", required = false) String password)
    throws UserAccessDeniedException {

    if (Strings.isBlank(username) || Strings.isBlank(password)) {
      throw new UserAccessDeniedException("用户名或密码为空");
    }

    Map<String, Object> claims = new HashMap<>();

    if (username.equals("wxj") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "wxj");
    } else if (username.equals("jason") && password.equals("123")) {
      claims.put(CommonConstants.TOKEN_USERNAME, "jason");
    } else {
      throw new UserAccessDeniedException("用户名或密码错误");
    }

    String token = JwtUtils.generateToken(jwtConfig.getSecretKey(), claims, 1);

    return ResponseResultWrappers.success(new HashMap<>() {{
      put("token", token);
    }});
  }
}
