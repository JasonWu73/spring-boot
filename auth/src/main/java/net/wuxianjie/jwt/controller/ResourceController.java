package net.wuxianjie.jwt.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/res")
public class ResourceController {

  /**
   * 匿名用户
   */
  @GetMapping(value = "/public", produces = MediaType.APPLICATION_JSON_VALUE)
  public String anonymous() {
    return "匿名用户";
  }

  /**
   * 来宾用户 (角色项为空)
   */
  @GetMapping(value = "/guest", produces = MediaType.APPLICATION_JSON_VALUE)
  public String guest(Authentication authentication) {
    return "通过身份验证的用户: " + authentication.getName();
  }

  /**
   * 普通用户
   */
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  @GetMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
  public String user(Authentication authentication) {
    return "普通用户: " + authentication.getName();
  }

  /**
   * 管理员
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(value = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
  public String admin(Authentication authentication) {
    return "管理员: " + authentication.getName();
  }
}
