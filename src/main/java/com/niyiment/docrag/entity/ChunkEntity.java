package com.niyiment.docrag.entity;


import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "chunks")
public class ChunkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String content;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String embeddingJson;

    public ChunkEntity() {
    }

    public ChunkEntity(Integer id, DocumentEntity document, String content, String embeddingJson) {
        this.id = id;
        this.document = document;
        this.content = content;
        this.embeddingJson = embeddingJson;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChunkEntity that = (ChunkEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(document, that.document) && Objects.equals(content, that.content) && Objects.equals(embeddingJson, that.embeddingJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, document, content, embeddingJson);
    }
}
