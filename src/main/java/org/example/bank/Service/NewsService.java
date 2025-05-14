package org.example.bank.Service;

import org.example.bank.DTO.NewsDTO;
import org.example.bank.DTO.NewsPreviewDTO;
import org.example.bank.Entity.News;
import org.example.bank.Repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {
    
    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public News createNews(NewsDTO newsDTO) {
        News news = new News();
        news.setTitle(newsDTO.getTitle());
        news.setPreview(newsDTO.getPreview());
        news.setFullContent(newsDTO.getFullContent());
        news.setImageUrl(newsDTO.getImageUrl());
        news.setPublicationDate(LocalDateTime.now());
        return newsRepository.save(news);
    }

    public List<NewsPreviewDTO> getAllNewsPreviews() {
        return newsRepository.findAll().stream()
                .map(this::convertToPreviewDTO)
                .collect(Collectors.toList());
    }

    public NewsDTO getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        return convertToDTO(news);
    }

    public News updateNews(Long id, NewsDTO newsDTO) {
        News news = newsRepository.findById(id).orElse(null);
        if (news != null) {
            news.setTitle(newsDTO.getTitle());
            news.setPreview(newsDTO.getPreview());
            news.setFullContent(newsDTO.getFullContent());
            news.setImageUrl(newsDTO.getImageUrl());
            return newsRepository.save(news);
        }
        return null;
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    public NewsDTO convertToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setPreview(news.getPreview());
        dto.setFullContent(news.getFullContent());
        dto.setImageUrl(news.getImageUrl());
        dto.setPublicationDate(news.getPublicationDate());
        return dto;
    }

    public NewsPreviewDTO convertToPreviewDTO(News news) {
        NewsPreviewDTO dto = new NewsPreviewDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setPreview(news.getPreview());
        dto.setImageUrl(news.getImageUrl());
        dto.setPublicationDate(news.getPublicationDate());
        return dto;
    }
} 