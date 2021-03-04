package net.wuxianjie.elasticsearch.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import net.wuxianjie.common.model.DataPaging;
import net.wuxianjie.elasticsearch.domain.News;

public interface NewsService {

  Map<String, Object> getNewsByElasticClient(int newsId) throws IOException;

  News getNews(int newsId);

  News saveNews(News news);

  List<News> getAllNewsList();

  DataPaging<List<News>> getNewsPaging(int page, int size);

  List<News> getNewsListByType(String type);
}
