package net.wuxianjie.common.util;

import net.wuxianjie.common.model.ResponseResult;

public class ResponseResultWrappers {

  public static ResponseResult<Object> success(final Object data) {
    final ResponseResult<Object> result = new ResponseResult<>();
    result.setStatus("success");
    result.setData(data);
    return result;
  }

  public static ResponseResult<Void> error(final String errorMsg) {
    final ResponseResult<Void> result = new ResponseResult<>();
    result.setStatus("error");
    result.setError(errorMsg);
    return result;
  }
}
