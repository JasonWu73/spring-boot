package net.wuxianjie.web.aspect;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.common.exception.AuthorizationException;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalControllerExceptionHandler {

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ResponseResult<Void>> handleHttpRequestMethodNotSupportedException(
    HttpRequestMethodNotSupportedException e) {
    log.warn("请求方法不支持: {}", e.getMessage());
    return new ResponseEntity<>(ResponseResultWrappers.error(e.getMessage()), HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ResponseResult<Void>> handleMissingRequestHeaderException(
    MissingRequestHeaderException e) {
    log.warn("缺少必要的请求头: {}", e.getMessage());
    return new ResponseEntity<>(ResponseResultWrappers.error("缺少必要的请求头"), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseResult<Void>> handleConstraintViolationException(
    ConstraintViolationException e) {

    List<String> errorsForLog = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      errorsForLog.add(violation.getRootBeanClass().getName() + " " +
        violation.getPropertyPath() + ": " + violation.getMessage());

      errors.add(violation.getMessage());
    }

    log.warn("参数错误: {}", String.join("; ", errorsForLog));

    return new ResponseEntity<>(ResponseResultWrappers.error(String.join("; ", errors))
      , HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ResponseResult<Void>> handleAuthenticationExceptionException(AuthenticationException e) {
    log.warn("身份验证失败: {}", e.getMessage());
    return new ResponseEntity<>(ResponseResultWrappers.error(e.getMessage()), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthorizationException.class)
  public ResponseEntity<ResponseResult<Void>> handleAuthorizationExceptionException(AuthorizationException e) {
    log.warn("授权认证失败: {}", e.getMessage());
    return new ResponseEntity<>(ResponseResultWrappers.error(e.getMessage()), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ResponseResult<Void>> handleThrowable(Throwable e) {
    log.error("默认异常处理", e);
    return new ResponseEntity<>(ResponseResultWrappers.error(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
