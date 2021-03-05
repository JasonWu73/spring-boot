package net.wuxianjie.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class JsonTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void parseJason() throws JsonProcessingException {
    final String test = "{\"status\":\"success\",\"data\":[{\"date\":\"2021-03-05 14:27:25\",\"title\":\"微服务搭建离不开网关\"},{\"date\":\"2021-03-04 14:27:25\",\"title\":\"技术在于点滴积累\"}]}";

    TypeReference<Map<String, Object>> ref = new TypeReference<>() {
    };
    final Map<String, Object> data = objectMapper.readValue(test, ref);

    log.info(data.toString());
  }
}
