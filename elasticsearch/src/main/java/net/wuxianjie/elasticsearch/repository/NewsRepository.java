package net.wuxianjie.elasticsearch.repository;

import java.util.List;
import net.wuxianjie.elasticsearch.domain.News;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsRepository extends ElasticsearchRepository<News, Integer> {

  @Query("{\"term\":{\"news_type\":{\"value\":\"?0\"}}}")
  List<News> findByType(String type);
}
