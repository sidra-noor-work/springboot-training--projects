package com.example.blogwebsitespringboot.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blogId; // renamed to avoid ShortVariable rule

    private String title;
    private String content;
    private String username;

    public Blog(final Long blogId, final String title, final String content) {
        this.blogId = blogId;
        this.title = title;
        this.content = content;
    }

    public Blog(final String title, final String content, final String username) {
        this.title = title;
        this.content = content;
        this.username = username;
    }

    public void setId(Long id) {
        this.blogId = id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
