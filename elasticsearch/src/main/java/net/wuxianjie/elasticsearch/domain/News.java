package net.wuxianjie.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@TypeAlias("News")
@Document(indexName = "news")
public class News {

  @Id
  private Integer id;

  @Field(name = "news_title", type = FieldType.Text)
  private String title;

  @Field(name = "news_type", type = FieldType.Keyword)
  private String type;

  @Field(name = "news_add_time", type = FieldType.Date,
      format = DateFormat.custom, pattern = "uuuu-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime addTime;
}
