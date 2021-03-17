package net.wuxianjie.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private Integer userId;

  private String username;

  private String password;

  /**
   * 角色列表字符串, 不包含 {@code ROLE_} 前缀
   */
  private String roles;
}
