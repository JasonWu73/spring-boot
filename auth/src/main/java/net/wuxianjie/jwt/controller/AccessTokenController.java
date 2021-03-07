package net.wuxianjie.jwt.controller;

import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.model.CreateToken;
import net.wuxianjie.jwt.model.ParseToken;
import net.wuxianjie.jwt.service.AccessTokenService;
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

  private final AccessTokenService tokenService;

  @PostMapping
  public CreateToken createToken(
    @NotBlank(message = "用户名不能为空") String username,
    @NotBlank(message = "密码不能为空") String password)
    throws AuthenticationException {

    return tokenService.getOrCreateToken(username, password);
  }

  @GetMapping("/verify")
  public ParseToken verifyToken(@NotBlank(message = "Token 不能为空") String token) {
    return tokenService.verifyToken(token);
  }
}
