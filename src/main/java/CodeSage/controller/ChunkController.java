package CodeSage.controller;

import CodeSage.model.CodeChunk;
import CodeSage.model.CodeFile;
import CodeSage.service.ChunkService;
import CodeSage.service.FileLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chunk")
public class ChunkController {

    private static final Logger log = LoggerFactory.getLogger(ChunkController.class);
    private final FileLoaderService fileLoaderService;
    private final ChunkService chunkService;

    public ChunkController(FileLoaderService fileLoaderService,
                           ChunkService chunkService) {
        this.fileLoaderService = fileLoaderService;
        this.chunkService = chunkService;
        log.info("ChunkController initialized");
    }

    @PostMapping
    public Map<String, Object> chunk(@RequestBody Map<String, String> req) {
        String path = req.get("path");
        log.info("Chunk request received for path: {}", path);
        try {
            List<CodeFile> files = fileLoaderService.loadFiles(path);
            log.info("Loaded {} files from path: {}", files.size(), path);

            List<CodeChunk> chunks = chunkService.chunk(files);
            log.info("Created {} chunks from {} files", chunks.size(), files.size());

            return Map.of(
                    "files", files.size(),
                    "chunks", chunks.size()
            );
        } catch (Exception e) {
            log.error("Error processing chunk request for path: {}", path, e);
            throw e;
        }
    }
}