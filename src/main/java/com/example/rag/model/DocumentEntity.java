package com.example.rag.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 10000)
    private String text;

    private String title;

    @Column(columnDefinition = "CLOB")
    private String embeddingJson;

    private String source;

    public DocumentEntity() {}

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEmbeddingJson() { return embeddingJson; }
    public void setEmbeddingJson(String embeddingJson) { this.embeddingJson = embeddingJson; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
