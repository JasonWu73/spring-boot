package net.wuxianjie.jwt.model;

import lombok.Data;

@Data
public class AuthData {

  private String token;

  private Integer userId;

  private String username;

  /**
   * 角色列表字符串, 不包含 {@code ROLE_} 前缀
   */
  private String roles;
}
