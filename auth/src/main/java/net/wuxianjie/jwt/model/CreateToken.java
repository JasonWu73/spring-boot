package net.wuxianjie.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateToken {

  /**
   * 剩余有效时间 (秒)
   */
  private Long expire;

  private String token;
}
