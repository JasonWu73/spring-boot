package net.wuxianjie.elasticsearch.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {

  /**
   * 该接口所需权限, 以 {@code ,} 分隔
   *
   * <p>多个项以<strong>或</strong>为逻辑组合</p>
   *
   * <p>默认为空, 代表所有用户都可以访问</p>
   */
  String value() default "";
}
