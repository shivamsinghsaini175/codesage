package CodeSage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OllamaEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(OllamaEmbeddingService.class);
    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:11434")
            .build();

    public OllamaEmbeddingService() {
        log.info("OllamaEmbeddingService initialized with base URL: http://localhost:11434");
    }

    public List<Double> embed(String text) {
        log.debug("Generating embedding for text of length: {} ", text.length());
        try {
            Map<String, Object> body = Map.of(
                    "model", "nomic-embed-text",
                    "prompt", text
            );

            List<Double> result = webClient.post()
                    .uri("/api/embeddings")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(res -> (List<Double>) res.get("embedding"))
                    .block();

            log.debug("Successfully generated embedding with {} dimensions", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("Failed to generate embedding for text", e);
            throw e;
        }
    }
}