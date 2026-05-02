package CodeSage.service;

import CodeSage.model.CodeChunk;
import CodeSage.model.CodeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkService {

    private static final Logger log = LoggerFactory.getLogger(ChunkService.class);
    private static final int CHUNK_SIZE = 1500;

    public List<CodeChunk> chunk(List<CodeFile> files) {
        log.info("Starting chunking process for {} files with chunk size: {}", files.size(), CHUNK_SIZE);
        List<CodeChunk> chunks = new ArrayList<>();

        for (CodeFile file : files) {
            String content = file.getContent();
            int fileChunks = 0;

            for (int i = 0; i < content.length(); i += CHUNK_SIZE) {
                int end = Math.min(i + CHUNK_SIZE, content.length());
                String part = content.substring(i, end);

                chunks.add(new CodeChunk(part, file.getPath()));
                fileChunks++;
            }
            
            log.debug("Created {} chunks from file: {}", fileChunks, file.getPath());
        }

        log.info("Chunking completed: Generated {} total chunks from {} files", chunks.size(), files.size());
        return chunks;
    }
}