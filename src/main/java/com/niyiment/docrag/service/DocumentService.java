package com.niyiment.docrag.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.niyiment.docrag.entity.ChunkEntity;
import com.niyiment.docrag.entity.DocumentEntity;
import com.niyiment.docrag.repository.ChunkRepository;
import com.niyiment.docrag.repository.DocumentRepository;
import com.niyiment.docrag.utils.ChunkUtils;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Service
public class DocumentService {
    Logger log = LoggerFactory.getLogger(DocumentService.class);
    private final Path storagePath;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int chunkSize;

    public DocumentService(
            @Value("${app.storage.path:./storage}") String storageDirectory,
            @Value("${app.chunk.size:1000}") int chunkSize,
            DocumentRepository documentRepository,
            ChunkRepository chunkRepository,
            EmbeddingModel embeddingModel
            ) throws IOException {
        this.storagePath = Paths.get(storageDirectory).toAbsolutePath().normalize();
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.embeddingModel = embeddingModel;
        this.chunkSize = chunkSize;
        Files.createDirectories(this.storagePath);
    }

    @Transactional
    public DocumentEntity storedPdf(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Invalid file, only PDF uploads are allowed");
        }
        String contentType = file.getContentType();
        if (contentType!= null && !contentType.startsWith("application/pdf")) {
            throw new IllegalArgumentException("Invalid file, only PDF uploads are allowed");
        }
        Path destination = storagePath.resolve(System.currentTimeMillis() + "-" + originalFilename);
        Files.copy(file.getInputStream(), destination);
        DocumentEntity document = new DocumentEntity();
        document.setFilename(originalFilename);
        document.setPath(destination.toString());
        documentRepository.save(document);

        String text = extractTextFromPdf(destination.toFile());
        List<String> chunks = ChunkUtils.chunkText(text, chunkSize, chunkSize / 2, true, true);

        log.info("Chunks: {}", chunks.getFirst());
        for(String chunk : chunks) {
            float[] embedding = embeddingModel.embed(chunk);
            String json = objectMapper.writeValueAsString(embedding);
            ChunkEntity chunkEntity = new ChunkEntity(document, chunk, json);
            chunkRepository.save(chunkEntity);
        }

        return document;
    }

    private String extractTextFromPdf(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(file))) {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(document);
        }
    }
}
