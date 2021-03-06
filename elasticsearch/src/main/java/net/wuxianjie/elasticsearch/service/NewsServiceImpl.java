package net.wuxianjie.elasticsearch.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.model.DataPaging;
import net.wuxianjie.elasticsearch.domain.News;
import net.wuxianjie.elasticsearch.repository.NewsRepository;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

  private final RestHighLevelClient client;

  private final NewsRepository repository;

  @Override
  public Map<String, Object> getNewsByElasticClient(int newsId) throws IOException {

    GetRequest request = new GetRequest().id(String.valueOf(newsId)).index("news");

    return client.get(request, RequestOptions.DEFAULT).getSource();
  }

  @Override
  public News getNews(int newsId) {

    Optional<News> optional = repository.findById(newsId);

    return optional.orElse(null);
  }

  @Override
  public News saveNews(News news) {

    repository.save(news);

    return news;
  }

  @Override
  public List<News> getAllNewsList() {

    Iterable<News> iterable = repository.findAll();

    List<News> list = new ArrayList<>();
    iterable.forEach(list::add);

    return list;
  }

  @Override
  public DataPaging<List<News>> getNewsPaging(int page, int size) {

    Page<News> news = repository.findAll(PageRequest.of(page, size));

    List<News> list = new ArrayList<>();
    news.forEach(list::add);

    return new DataPaging<>() {{
      setCurrent(page + 1);
      setSize(size);
      setList(list);
      setTotal(news.getTotalElements());
    }};
  }

  @Override
  public List<News> getNewsListByType(String type) {
    return repository.findByType(type);
  }
}
