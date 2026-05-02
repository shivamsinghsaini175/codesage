package CodeSage.service;

import CodeSage.entity.CodeChunkEntity;
import CodeSage.model.CodeChunk;
import CodeSage.repository.CodeChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VectorStoreService {

    @Autowired
    private CodeChunkRepository repo;

    @Autowired
    private OllamaEmbeddingService embeddingService;

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);
    private final EmbeddingModel embeddingModel;
    private final Map<String, EmbeddedChunk> store = new HashMap<>();

    public VectorStoreService(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        log.info("VectorStoreService initialized with Ollama embedding model");
    }

    public void store(List<CodeChunk> chunks) {

        for (CodeChunk chunk : chunks) {

            List<Double> vec = embeddingService.embed(chunk.getContent());

            float[] embedding = new float[vec.size()];
            for (int i = 0; i < vec.size(); i++) {
                embedding[i] = vec.get(i).floatValue();
            }

            CodeChunkEntity entity = new CodeChunkEntity();
            entity.setContent(chunk.getContent());
            entity.setFilePath(chunk.getFilePath());
            entity.setEmbedding(embedding);

            repo.save(entity);
        }
    }


    public List<Map<String, String>> searchDb(String query, int limit) {

        List<Double> vec = embeddingService.embed(query);

        String vector = vec.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        List<Object[]> rows = repo.searchRaw(vector, limit);

        return rows.stream()
                .map(r -> Map.of(
                        "content", (String) r[0],
                        "path", (String) r[1]
                ))
                .toList();
    }


    public List<EmbeddedChunk> search(String query, int limit) {
        log.info("Searching for query: '{}' with limit: {}", query, limit);
        try {
            float[] queryEmbedding = embeddingModel.embed(query);
            log.debug("Generated embedding for query");

            List<EmbeddedChunk> results = store.values()
                    .stream()
                    .map(chunk -> new AbstractMap.SimpleEntry<>(
                            chunk,
                            cosineSimilarity(queryEmbedding, chunk.embedding)
                    ))
                    .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                    .limit(limit)
                    .map(AbstractMap.SimpleEntry::getKey)
                    .toList();

            log.info("Search completed: returned {} results for query '{}'", results.size(), query);
            return results;
        } catch (Exception e) {
            log.error("Error searching for query: '{}' ", query, e);
            throw new RuntimeException("Failed to search: " + e.getMessage(), e);
        }
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0f;
        float normA = 0f;
        float normB = 0f;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dotProduct / ((float) Math.sqrt(normA) * (float) Math.sqrt(normB));
    }

    public static class EmbeddedChunk {
        public final String content;
        public final String filePath;
        public final float[] embedding;

        public EmbeddedChunk(String content, String filePath, float[] embedding) {
            this.content = content;
            this.filePath = filePath;
            this.embedding = embedding;
        }
    }
}