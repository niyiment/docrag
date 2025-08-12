package com.niyiment.docrag.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.niyiment.docrag.entity.ChunkEntity;
import com.niyiment.docrag.repository.ChunkRepository;
import com.niyiment.docrag.utils.ChunkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class QAService {
    Logger log = LoggerFactory.getLogger(QAService.class);
    private final ChunkRepository chunkRepository;
    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int topK;

    public QAService(ChunkRepository chunkRepository,
                     EmbeddingModel embeddingModel,
                     ChatModel chatModel,
                     @Value("${app.top.k:4}") int topK) {
        this.chunkRepository = chunkRepository;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.topK = topK;
    }

    public String answerQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            return "Question is required";
        }

        float[] qVector = embeddingModel.embed(question);
        List<ChunkEntity> all = chunkRepository.findAll();

        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (ChunkEntity chunkEntity : all) {
            try {
                float[] vector = objectMapper.readValue(chunkEntity.getEmbeddingJson(), float[].class);
                double score = ChunkUtils.cosineSimilarity(qVector, vector);
                scoredChunks.add(new ScoredChunk(chunkEntity, score));
            } catch (Exception e) {
                log.error("Error reading embedded json data: {}", e.getMessage());
            }
        }

        List<String> contexts = scoredChunks.stream()
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .map(ScoredChunk::chunkEntity)
                .map(ChunkEntity::getContent)
                .toList();

        String context = String.join("\n\n---\n\n", contexts);
        String prompt = """
                You are a helpful assistant specialized in answering questions using the provided document context.
                Use only the context below to answer the user's question. If the answer is not present in the context, say you don't know and do not hallucinate.

                CONTEXT:
                %s

                QUESTION:
                %s

                Provide a concise, accurate answer and cite (briefly) when useful.
                """.formatted(context, question);
        String reply = chatModel.call(prompt);
        return reply;
    }

    private record ScoredChunk(ChunkEntity chunkEntity, double score) {}

}
