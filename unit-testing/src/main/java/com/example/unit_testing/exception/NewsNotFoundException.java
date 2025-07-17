package com.example.unit_testing.exception;

public class NewsNotFoundException extends RuntimeException {
    public NewsNotFoundException(int id) {
        super("News with ID " + id + " not found.");
    }
}
