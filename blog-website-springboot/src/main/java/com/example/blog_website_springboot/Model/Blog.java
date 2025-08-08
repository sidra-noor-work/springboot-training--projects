package com.example.blog_website_springboot.Model;

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
    private Long id;
    private String title;
    private String content;
    private String username;
    public Blog(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
    public Blog(String title, String content, String username) {
        this.title = title;
        this.content = content;
        this.username = username;
    }
}
