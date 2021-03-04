package net.wuxianjie.common.exception;

public class UserAccessDeniedException extends Exception {

  public UserAccessDeniedException(String message) {
    super(message);
  }

  public UserAccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }
}
