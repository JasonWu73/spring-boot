package net.wuxianjie.common.constant;

public class CommonConstants {

  /**
   * JWT 中的用户名声明字段
   */
  public static final String TOKEN_USERNAME = "user";

  /**
   * 网关验证 Token 通过后, 传递给下游服务的请求头
   */
  public static final String REQUEST_HEADER_USERNAME = "CURRENT_USER";

  /**
   * 网关验证 Token 不通过时, 跳转的地址
   */
  public static final String PATH_UNAUTHORIZED = "/unauthorized";
}
