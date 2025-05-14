package org.example.bank.Controller;

import org.example.bank.DTO.NewsDTO;
import org.example.bank.DTO.NewsPreviewDTO;
import org.example.bank.Entity.News;
import org.example.bank.Service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NewsDTO> createNews(@RequestBody NewsDTO newsDTO) {
        News createdNews = newsService.createNews(newsDTO);
        return ResponseEntity.ok(newsService.convertToDTO(createdNews));
    }

    @GetMapping("/preview")
    public ResponseEntity<List<NewsPreviewDTO>> getAllNewsPreviews() {
        List<NewsPreviewDTO> news = newsService.getAllNewsPreviews();
        return ResponseEntity.ok(news);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        NewsDTO news = newsService.getNewsById(id);
        return ResponseEntity.ok(news);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NewsDTO> updateNews(@PathVariable Long id, @RequestBody NewsDTO newsDTO) {
        News updatedNews = newsService.updateNews(id, newsDTO);
        if (updatedNews != null) {
            return ResponseEntity.ok(newsService.convertToDTO(updatedNews));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return ResponseEntity.ok().build();
    }
} 