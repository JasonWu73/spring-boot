package net.wuxianjie.common.util;

import net.wuxianjie.common.model.ResponseResult;

public class ResponseResultWrappers {

  public static ResponseResult<Object> success(Object data) {

    ResponseResult<Object> result = new ResponseResult<>();

    result.setStatus("success");
    result.setData(data);

    return result;
  }

  public static ResponseResult<Void> error(String errorMsg) {

    ResponseResult<Void> result = new ResponseResult<>();

    result.setStatus("error");
    result.setError(errorMsg);

    return result;
  }
}
