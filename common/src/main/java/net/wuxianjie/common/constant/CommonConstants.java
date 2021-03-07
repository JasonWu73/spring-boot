package net.wuxianjie.common.constant;

public class CommonConstants {

  /**
   * 网关验证 Token 通过后, 传递给下游服务的请求头
   */
  public static final String REQUEST_HEADER_USERNAME = "CURRENT_USER";

  /**
   * 网关验证 Token 通过后, 传递给下游服务的请求头
   */
  public static final String REQUEST_HEADER_DEVELOPER = "DEVELOPER";

  /**
   * 网关验证 Token 不通过时, 跳转的地址
   */
  public static final String PATH_UNAUTHENTICATED = "/unauthenticated";

  /**
   * 网关测试所用域名, 改本地 hosts 文件
   */
  public static final String DOMAIN_NAME = "http://api.wxj.com/";

  /**
   * Token 验证服务地址
   */
  public static final String URL_TOKEN_VERIFY = "http://localhost:8080/access_token/verify?token=";
}
