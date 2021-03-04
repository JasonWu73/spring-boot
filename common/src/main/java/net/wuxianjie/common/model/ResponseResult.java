package net.wuxianjie.common.model;

import lombok.Data;

@Data
public class ResponseResult<T> {

  private String status;
  private String error; // 若请求成功, 则不需要返回该字段给前端
  private T data; // 若请求失败, 则不需要返回该字段给前端
}
