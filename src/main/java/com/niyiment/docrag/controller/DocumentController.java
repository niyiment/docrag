package com.niyiment.docrag.controller;


import com.niyiment.docrag.entity.DocumentEntity;
import com.niyiment.docrag.repository.DocumentRepository;
import com.niyiment.docrag.service.DocumentService;
import com.niyiment.docrag.service.QAService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final QAService qaService;

    public DocumentController(DocumentService documentService,
                              DocumentRepository documentRepository,
                              QAService qaService) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
        this.qaService = qaService;
    }

    @RequestMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        try {
            DocumentEntity document = documentService.storedPdf(file);
            return ResponseEntity.ok(Map.of("id", document.getId(), "filename", document.getFilename()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
        } catch (IOException exception) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to process file", "message",exception.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentEntity>> listDocuments() {
        return ResponseEntity.ok(documentRepository.findAll());
    }

    @PostMapping("/query")
    public ResponseEntity<?> query(@RequestBody Map<String, String> request) {
        try {
            String question = request.get("question");
            if (question == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
            }
            String answer = qaService.answerQuestion(question);
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to process request", "message",exception.getMessage()));
        }

    }

}
