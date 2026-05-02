package CodeSage.controller;

import CodeSage.model.CodeChunk;
import CodeSage.model.CodeFile;
import CodeSage.service.ChunkService;
import CodeSage.service.FileLoaderService;
import CodeSage.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/embed")
public class EmbedController {

    private static final Logger log = LoggerFactory.getLogger(EmbedController.class);
    private final FileLoaderService fileLoaderService;
    private final ChunkService chunkService;
    private final VectorStoreService vectorStoreService;

    public EmbedController(FileLoaderService fileLoaderService,
                           ChunkService chunkService,
                           VectorStoreService vectorStoreService) {
        this.fileLoaderService = fileLoaderService;
        this.chunkService = chunkService;
        this.vectorStoreService = vectorStoreService;
        log.info("EmbedController initialized");
    }

    @PostMapping
    public Map<String, Object> embed(@RequestBody Map<String, String> req) {
        String path = req.get("path");
        log.info("Embed request received for path: {}", path);
        try {
            List<CodeFile> files = fileLoaderService.loadFiles(path);
            log.info("Loaded {} files from path: {}", files.size(), path);

            List<CodeChunk> chunks = chunkService.chunk(files);
            log.info("Created {} chunks from files", chunks.size());

            vectorStoreService.store(chunks);
            log.info("Stored {} chunks in vector store", chunks.size());

            return Map.of(
                    "files", files.size(),
                    "chunks", chunks.size(),
                    "status", "stored"
            );
        } catch (Exception e) {
            log.error("Error processing embed request for path: {}", path, e);
            throw e;
        }
    }

    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String, Object> req) {
        String query = (String) req.get("query");
        int limit = req.containsKey("limit") ? (int) req.get("limit") : 5;

        log.info("Search request received with query: {} and limit: {}", query, limit);
        try {
            List<VectorStoreService.EmbeddedChunk> results = vectorStoreService.search(query, limit);
            log.info("Found {} results for query: {}", results.size(), query);

            return Map.of(
                    "query", query,
                    "results", results.stream()
                            .map(chunk -> Map.of(
                                    "filePath", chunk.filePath,
                                    "content", chunk.content.substring(0, Math.min(100, chunk.content.length())) + "..."
                            ))
                            .toList()
            );
        } catch (Exception e) {
            log.error("Error processing search request with query: {}", query, e);
            throw e;
        }
    }
}