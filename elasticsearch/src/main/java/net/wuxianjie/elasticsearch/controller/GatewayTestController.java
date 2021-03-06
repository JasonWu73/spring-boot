package net.wuxianjie.elasticsearch.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.elasticsearch.annotation.Auth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/gw")
public class GatewayTestController {

  @Auth("wxj")
  @GetMapping("/wxj")
  public Map<String, Object> wxj(
    @RequestHeader(CommonConstants.REQUEST_HEADER_USERNAME) String user,
    @RequestHeader(CommonConstants.REQUEST_HEADER_DEVELOPER) String developer
  ) throws AuthenticationException {

    return new HashMap<>() {{
      put("developer", developer);
      put("user", user);
      put("info", "只允许 wxj 访问");
    }};
  }

  @Auth("jason")
  @GetMapping("/jason")
  public Map<String, Object> jason(
    @RequestHeader(CommonConstants.REQUEST_HEADER_USERNAME) String user,
    @RequestHeader(CommonConstants.REQUEST_HEADER_DEVELOPER) String developer
  ) throws AuthenticationException {

    return new HashMap<>() {{
      put("developer", developer);
      put("user", user);
      put("info", "只允许 jason 访问");
    }};
  }

  @Auth("wxj,jason")
  @GetMapping("/wj")
  public Map<String, Object> wj(
    @RequestHeader(CommonConstants.REQUEST_HEADER_USERNAME) String user,
    @RequestHeader(CommonConstants.REQUEST_HEADER_DEVELOPER) String developer
  ) throws AuthenticationException {

    return new HashMap<>() {{
      put("developer", developer);
      put("user", user);
      put("info", "wxj 和 jason 都可访问");
    }};
  }

  @GetMapping("/all")
  public Map<String, Object> all(
    @RequestHeader(CommonConstants.REQUEST_HEADER_USERNAME) String user,
    @RequestHeader(CommonConstants.REQUEST_HEADER_DEVELOPER) String developer
  ) throws AuthenticationException {

    return new HashMap<>() {{
      put("developer", developer);
      put("user", user);
      put("info", "谁都可以访问");
    }};
  }
}
