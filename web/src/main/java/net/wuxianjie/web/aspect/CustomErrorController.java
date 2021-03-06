package net.wuxianjie.web.aspect;

import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import net.wuxianjie.common.model.ResponseResult;
import net.wuxianjie.common.util.ResponseResultWrappers;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

  private final ErrorAttributes errorAttributes;

  public CustomErrorController(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  @RequestMapping
  @ResponseBody
  public ResponseEntity<ResponseResult<Void>> handleErrorResponse(WebRequest request) {

    Map<String, Object> errorMap = errorAttributes.getErrorAttributes(request,
      ErrorAttributeOptions.defaults());

    Integer httpStatus = (Integer) errorMap.get("status");
    String error = (String) errorMap.get("error");

    if (httpStatus == HttpStatus.NOT_FOUND.value()) {
      log.warn("全局异常处理: {}", errorMap);
    } else {
      log.error("全局异常处理: {}", errorMap);
    }

    return new ResponseEntity<>(ResponseResultWrappers.error(error),
      Objects.requireNonNull(HttpStatus.resolve(httpStatus)));
  }

  @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
  public String errorHtml() {
    return "redirect:/";
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }
}
