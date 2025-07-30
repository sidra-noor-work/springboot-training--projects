package com.example.blog_website_springboot.Repository;

import com.example.blog_website_springboot.Model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByUsername(String username);
    Optional<AppUser> findByUsername(String username);
}
