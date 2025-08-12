package com.niyiment.docrag.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ChunkUtils  {

    private ChunkUtils() {}

    public static double cosineSimilarity(float[] qVector, float[] vector) {
        if (qVector == null || vector == null || qVector.length != vector.length) return -1.0;

        double dot = 0.0;
        double nQvector = 0.0;
        double nVector = 0.0;
        for (int i = 0; i < qVector.length; i++) {
            dot += (double) qVector[i] * (double) vector[i];
            nQvector += (double) qVector[i] * (double) qVector[i];
            nVector += (double) vector[i] * (double) vector[i];
        }
        double denom = Math.sqrt(nQvector) * Math.sqrt(nVector);
        if (denom == 0.0) return 0.0;
        return dot / denom;
    }

    private static record TextChunk(int index, String text, int start, int end){}

    @SuppressWarnings("unchecked")
    public static <T> List<T> chunkText(String text, int chunkSize, int overlap, boolean
                                        breakOnWords, boolean withMetadata) {
        List<T> chunks = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return chunks;
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be > 0");
        }
        if (overlap < 0) {
            throw new IllegalArgumentException("Overlap must be >= 0");
        }

        int start = 0;
        int chunkIndex = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            if (breakOnWords && end < text.length()) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start + chunkSize / 2) {
                    end = lastSpace;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {

                    chunks.add((T) chunk);
            }
            start = Math.max(start + chunkSize - overlap, end);
        }

        return chunks;
    }
}
