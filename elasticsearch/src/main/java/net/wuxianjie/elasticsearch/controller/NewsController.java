package net.wuxianjie.elasticsearch.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.wuxianjie.common.model.DataPaging;
import net.wuxianjie.elasticsearch.domain.News;
import net.wuxianjie.elasticsearch.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

  private final NewsService service;

  @GetMapping("/cl/{newsId:\\d+}")
  public Map<String, Object> searchNewsByElasticsearchClient(
    @PathVariable int newsId) throws IOException {

    return service.getNewsByElasticClient(newsId);
  }

  @GetMapping("/{newsId:\\d+}")
  public News searchNewsById(@PathVariable int newsId) {
    return service.getNews(newsId);
  }

  @PostMapping("/{newsId:\\d+}")
  public News addNews(@PathVariable int newsId, @RequestBody News news) {

    news.setId(newsId);
    return service.saveNews(news);
  }

  @GetMapping("/all")
  public List<News> listAllNews() {
    return service.getAllNewsList();
  }

  @GetMapping
  public DataPaging<List<News>> listNewsPaging(
    @RequestParam(value = "p", required = false, defaultValue = "1") int page,
    @RequestParam(value = "s", required = false, defaultValue = "2") int size
  ) {

    if (page > 0) {
      page--;
    }

    return service.getNewsPaging(page, size);
  }

  @GetMapping("/c/{newsType:[\u4e00-\u9fa5\\w]+}")
  public List<News> listNewsByType(@PathVariable("newsType") String type) {
    return service.getNewsListByType(type);
  }
}
