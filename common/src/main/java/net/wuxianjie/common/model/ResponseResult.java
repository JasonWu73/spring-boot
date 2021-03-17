package net.wuxianjie.common.model;

import lombok.Data;

@Data
public class ResponseResult<T> {

  /**
   * <ul>
   *   <li>成功: {@code success}</li>
   *   <li>失败: {@code fail}</li>
   * </ul>
   */
  private String status;

  /**
   * 失败时提示信息
   */
  private String message;

  /**
   * 结果字段
   */
  private T data;
}
