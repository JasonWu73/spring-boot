package net.wuxianjie.gateway.controller;

import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

  @RequestMapping("/fallback")
  public ResponseEntity<ResponseResult<Void>> fallback() {
    return new ResponseEntity<>(ResponseResultWrappers.error("糟糕身份验证服务出现了点问题 :("),
      HttpStatus.SERVICE_UNAVAILABLE);
  }

  @RequestMapping(CommonConstants.PATH_UNAUTHENTICATED)
  public ResponseEntity<ResponseResult<Void>> unauthorized() {
    return new ResponseEntity<>(ResponseResultWrappers.error("你不要乱搞哦 :)"),
      HttpStatus.UNAUTHORIZED);
  }
}
