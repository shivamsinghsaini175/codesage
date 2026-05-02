package CodeSage.controller;

import CodeSage.service.VectorStoreService;
import CodeSage.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ask")
public class AskController {

    private static final Logger log = LoggerFactory.getLogger(AskController.class);
    private final VectorStoreService vectorStoreService;
    private final AiService aiService;

    public AskController(VectorStoreService vectorStoreService,
                         AiService aiService) {
        this.vectorStoreService = vectorStoreService;
        this.aiService = aiService;
        log.info("AskController initialized");
    }

    @PostMapping
    public Map<String, String> ask(@RequestBody Map<String, String> req) {
        String question = req.get("question");
        log.info("Ask request received with question: {}", question);

        try {
            // Search for relevant code chunks
            log.debug("Searching for relevant code chunks...");
            var results = vectorStoreService.searchDb(question, 10);
            log.info("Found {} relevant chunks", results.size());

            String context = results.stream()
                    .map(r -> r.get("content"))
                    .collect(Collectors.joining("\n\n"));
            log.debug("Context prepared with {} characters", context.length());

            String prompt = """
                You are analyzing a Java Spring Boot project.

                Answer based only on the following code context.

                Focus on:
                - controllers
                - request mappings
                - APIs

                Context:
                %s

                Question: %s
                """.formatted(context, question);

            log.debug("Generating answer for question: {}", question);
            String answer = aiService.generate(prompt);

            log.info("Answer generated successfully for question: {}", question);
            return Map.of("answer", answer);
        } catch (Exception e) {
            log.error("Error processing ask request for question: {}", question, e);
            throw e;
        }
    }
}