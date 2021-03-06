package net.wuxianjie.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class HttpStatusException extends RuntimeException {

  @Getter
  private final HttpStatus httpStatus;

  public HttpStatusException(HttpStatus httpStatus, String message) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public HttpStatusException(HttpStatus httpStatus, String message, Throwable cause) {
    super(message, cause);
    this.httpStatus = httpStatus;
  }
}
