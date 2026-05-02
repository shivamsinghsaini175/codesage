package CodeSage.service;

import CodeSage.service.AiService;
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
    private AiService aiService;

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

    public List<Map<String, Object>> searchSmart(String query, int maxLimit) {

        List<Double> vec = embeddingService.embed(query);

        String vector = vec.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));

        List<Object[]> rows = repo.searchRaw(vector, maxLimit);

        List<Map<String, Object>> results = new ArrayList<>();

        double threshold = 18; // tune this
        int maxChunks = 10;
        int tokenLimit = 8000; // approx

        int currentTokens = 0;

        for (Object[] row : rows) {

            String content = row[0] != null ? row[0].toString() : "";
            String path = row[1] != null ? row[1].toString() : "";
            double score = ((Number) row[2]).doubleValue();

            System.out.println("DEBUG SCORE: " + score + " PATH: " + path);

            int tokens = content.length() / 4;

            if (currentTokens + tokens > tokenLimit) break;

            Map<String, Object> chunk = new HashMap<>();
            chunk.put("content", content);
            chunk.put("path", path);
            chunk.put("score", score);

            results.add(chunk);
            currentTokens += tokens;

            if (results.size() >= maxChunks) break;
        }
        return results;
    }

    public String rerankContext(String question, String context) {

        String prompt = """
You are given code snippets.

ONLY return relevant code parts.
DO NOT explain anything.
DO NOT answer the question.

Question:
%s

Code:
%s
""".formatted(question, context);

        return aiService.generate(prompt);
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