package com.example.springai_seminar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.document.Document;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "Book", schema = "spring_ai")
public class Book {
    @Id
    @Column(name = "Id", nullable = false, length = 36)
    private String id;

    @Column(name = "Title")
    private String title;

    @Column(name = "Author")
    private String author;

    @Column(name = "Description")
    private String description;

    public Document toDocument() {
        String content = String.format("Title: %s\nAuthor: %s\nDescription: %s",
                title, author, description);

        Map<String, Object> metadata = Map.of(
                "id", this.id,
                "title", this.title,
                "author", this.author,
                "description", this.description
        );

        return new Document(content, metadata);
    }
}