package net.wuxianjie.jwt;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class PasswordTest {

  @Test
  public void generatePassword() {
    String rawPassword = "123";
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    log.info("{} -> {}", rawPassword, passwordEncoder.encode(rawPassword));
  }
}
