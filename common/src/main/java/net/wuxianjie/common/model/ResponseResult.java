package net.wuxianjie.common.model;

import lombok.Data;

@Data
public class ResponseResult<T> {

  /**
   * <ul>
   *   <li>成功: {@code success}</li>
   *   <li>失败: {@code error}</li>
   * </ul>
   */
  private String status;

  /**
   * 错误信息
   */
  private String error;

  /**
   * 结果字段
   */
  private T data;
}
