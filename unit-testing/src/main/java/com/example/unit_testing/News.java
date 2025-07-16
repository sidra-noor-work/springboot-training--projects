package com.example.unit_testing;

import java.time.LocalDateTime;

public class News {
    private int newsId;
    private String title;
    private String details;
    private String reportedBy;
    private LocalDateTime reportedAt;

    public News(int newsId, String title, String details, String reportedBy, LocalDateTime reportedAt) {
        this.newsId = newsId;
        this.title = title;
        this.details = details;
        this.reportedBy = reportedBy;
        this.reportedAt = reportedAt;
    }

    public int getNewsId() {
        return newsId;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
}
