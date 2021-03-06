package net.wuxianjie.elasticsearch.aspect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import net.wuxianjie.common.constant.CommonConstants;
import net.wuxianjie.common.exception.AuthorizationException;
import net.wuxianjie.elasticsearch.annotation.Auth;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthorizationAspect {

  @Pointcut("@annotation(net.wuxianjie.elasticsearch.annotation.Auth)")
  public void pointcut() {

  }

  @Around("pointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    // 获取权限列表
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Auth auth = method.getAnnotation(Auth.class);
    ArrayList<String> authList = new ArrayList<>(Arrays.asList(auth.value().split(",")));

    // 获取网关验证通过后设定的请求头
    HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    String username = request.getHeader(CommonConstants.REQUEST_HEADER_USERNAME);

    // 权限为或逻辑, 即只要当前用户存在一项该权限即可访问
    // 这里只模拟判断用户名
    if (!authList.contains(username)) {
      throw new AuthorizationException("用户权限不足");
    }

    return joinPoint.proceed();
  }
}
