package net.wuxianjie.elasticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = "net.wuxianjie")
public class EsApplication {

  public static void main(String[] args) {
    SpringApplication.run(EsApplication.class, args);
  }
}
