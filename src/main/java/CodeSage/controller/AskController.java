package CodeSage.controller;

import CodeSage.service.VectorStoreService;
import CodeSage.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
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
    public Map<String, Serializable> ask(@RequestBody Map<String, String> req) {
        String question = req.get("question");
        log.info("Ask request received with question: {}", question);

        try {
            // Search for relevant code chunks
            log.debug("Searching for relevant code chunks...");
            var results = vectorStoreService.searchSmart(question, 20);

            results.forEach(r -> System.out.println("DEBUG PATH: " + r.get("path")));

            List<String> files = results.stream()
                    .map(r -> (String) r.get("path"))
                    .distinct()
                    .toList();

            log.info("Found {} relevant chunks", results.size());

            String context = results.stream()
                    .map(r -> "FILE: " + r.get("path") + "\nCODE:\n" + r.get("content"))
                    .collect(Collectors.joining("\n\n"));

            context = vectorStoreService.rerankContext(question, context);

            log.debug("Context prepared with {} characters", context.length());

            String prompt = """
You are analyzing backend code.

STRICT RULES:
- Answer ONLY using the provided code
- DO NOT assume anything
- MUST include file path from context
- Keep answer concise and structured

Context:
%s

Question:
%s
""".formatted(context, question);

            log.debug("Generating answer for question: {}", question);
            String answer = aiService.generate(prompt);

            log.info("Answer generated successfully for question: {}", question);
            int limit = 500;

            boolean truncated = context.length() > limit;

            String preview = truncated
                    ? context.substring(0, limit) + "..."
                    : context;

            return Map.of(
                    "answer", answer,
                    "context", preview,
                    "files", (Serializable) files,
                    "truncated", truncated
            );
        } catch (Exception e) {
            log.error("Error processing ask request for question: {}", question, e);
            throw e;
        }
    }
}