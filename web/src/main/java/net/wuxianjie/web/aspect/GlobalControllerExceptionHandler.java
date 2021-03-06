package net.wuxianjie.web.aspect;

import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.exception.UserAccessDeniedException;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {

  @ExceptionHandler(UserAccessDeniedException.class)
  public ResponseEntity<ResponseResult<Void>> handleUserAccessDeniedException(Exception e) {
    return new ResponseEntity<>(ResponseResultWrappers.error(e.getMessage()), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ResponseResult<Void>> handleThrowable(Throwable e) {
    log.error(e.getMessage(), e);
    return new ResponseEntity<>(ResponseResultWrappers.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}