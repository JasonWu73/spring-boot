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

  @RequestMapping(CommonConstants.PATH_UNAUTHORIZED)
  public ResponseEntity<ResponseResult<Void>> unauthorized() {
    return new ResponseEntity<>(ResponseResultWrappers.error("你不要乱搞哦"), HttpStatus.UNAUTHORIZED);
  }
}
