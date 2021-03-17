package net.wuxianjie.common.util;

import net.wuxianjie.common.model.ResponseResult;

public class ResponseResultWrappers {

  public static final String STATUS_SUCCESS = "success";
  public static final String STATUS_FAIL = "fail";

  public static <T> ResponseResult<T> success(T data) {

    ResponseResult<T> result = new ResponseResult<>();

    result.setStatus(STATUS_SUCCESS);
    result.setData(data);

    return result;
  }

  public static ResponseResult<Void> fail(String failMsg) {

    ResponseResult<Void> result = new ResponseResult<>();

    result.setStatus(STATUS_FAIL);
    result.setMessage(failMsg);

    return result;
  }
}
