package net.wuxianjie.jwt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.model.Token;
import net.wuxianjie.jwt.model.TokenData;
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
  public Token createToken(
    @NotBlank(message = "用户名不能为空") String username,
    @NotBlank(message = "密码不能为空") String password)
    throws AuthenticationException, JsonProcessingException {

    return tokenService.getOrCreateToken(username, password);
  }

  @PostMapping("/refresh")
  public Token refreshToken(@NotBlank(message = "Token 不能为空") String token) throws JsonProcessingException {
    return tokenService.refreshToken(token);
  }

  @GetMapping("/verify")
  public TokenData verifyToken(@NotBlank(message = "Token 不能为空") String token) throws JsonProcessingException {
    return tokenService.verifyToken(token);
  }
}
