package controllers;

import java.util.List;

import dao.NewsDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import models.News;

public class NewsListController {
    @FXML
    private ListView<News> newsListView;
    
    private NewsDAO newsDAO;
    
    public void initialize() {
        newsDAO = new NewsDAO();
        loadNews();
    }
    
    private void loadNews() {
        List<News> newsList = newsDAO.getAllNews();
        newsListView.getItems().addAll(newsList);
    }
    
    @FXML
    private void showNewsDetail(News news) {
        // Implementasi untuk membuka halaman detail berita
    }
} 