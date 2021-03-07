package net.wuxianjie.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Token {

  /**
   * 剩余有效时间 (秒)
   */
  private Long expire;

  private String token;

  /**
   * 只用于刷新的 Token. 可避免再次输入用户名和密码
   */
  private String refreshToken;
}
