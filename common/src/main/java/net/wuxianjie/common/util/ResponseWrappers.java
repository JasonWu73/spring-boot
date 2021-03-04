package net.wuxianjie.common.util;

import net.wuxianjie.common.model.ResponseResult;

public class ResponseWrappers {

  public static ResponseResult<Object> success(Object data) {

    final ResponseResult<Object> result = new ResponseResult<>();
    result.setStatus("success");
    result.setData(data);

    return result;
  }

  public static ResponseResult<Void> error(String errorMsg) {

    final ResponseResult<Void> result = new ResponseResult<>();
    result.setStatus("error");
    result.setError(errorMsg);

    return result;
  }
}
