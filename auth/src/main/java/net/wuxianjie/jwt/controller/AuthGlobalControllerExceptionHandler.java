package net.wuxianjie.jwt.controller;

import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthGlobalControllerExceptionHandler {

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseResult<Void>> handleAccessDeniedHandler() {
    return new ResponseEntity<>(ResponseResultWrappers.fail("没有访问权限"), HttpStatus.FORBIDDEN);
  }
}
