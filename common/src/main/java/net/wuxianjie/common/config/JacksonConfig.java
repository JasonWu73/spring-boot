package net.wuxianjie.common.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  public static final String DATE_FORMAT = "yyyy-MM-dd";

  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    return builder -> {
      builder.serializationInclusion(Include.NON_NULL);
      builder.timeZone("Asia/Shanghai");
      builder.simpleDateFormat(DATE_TIME_FORMAT);
      builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
      builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    };
  }

  @Bean
  public ObjectMapper objectMapper() {

    ObjectMapper mapper = new ObjectMapper();

    // property 命名转换仅对 POJO 有效，故对 `java.util.Map` 是无效的
    //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    mapper.setDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT));

    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDate.class,
      new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
    javaTimeModule.addSerializer(LocalDateTime.class,
      new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
    mapper.registerModule(javaTimeModule);

    return mapper;
  }
}
