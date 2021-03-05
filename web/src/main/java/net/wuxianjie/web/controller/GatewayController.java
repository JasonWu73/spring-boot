package net.wuxianjie.web.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.UserAccessDeniedException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/gw")
public class GatewayController {

  @GetMapping("/news")
  public List<Map<String, Object>> newsList(
    @RequestHeader(value = CommonConstants.REQUEST_HEADER_USERNAME, required = false) final String user,
    @RequestHeader(value = "developer", required = false) final String developer
  ) throws UserAccessDeniedException {
    log.info("开发者 -> {}", developer);
    log.info("用户 -> {}", user);

    if (Strings.isBlank(user)) {
      throw new UserAccessDeniedException("拒绝访问");
    }

    return new ArrayList<>() {{
      add(new HashMap<>() {{
        put("title", "微服务搭建离不开网关");
        put("date", LocalDateTime.now());
      }});
      add(new HashMap<>() {{
        put("title", "技术在于点滴积累");
        put("date", LocalDateTime.now().plusDays(-1));
      }});
    }};
  }
}
