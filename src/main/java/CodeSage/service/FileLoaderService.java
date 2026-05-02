package CodeSage.service;

import CodeSage.model.CodeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileLoaderService {

    private static final Logger log = LoggerFactory.getLogger(FileLoaderService.class);

    public List<CodeFile> loadFiles(String repoPath) {
        log.info("Loading code files from path: {}", repoPath);
        List<CodeFile> files = new ArrayList<>();

        try {
            Files.walk(Paths.get(repoPath))
                    .filter(Files::isRegularFile)
                    .filter(p -> isCodeFile(p.toString()))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p);
                            files.add(new CodeFile(p.toString(), content));
                            log.debug("Loaded file: {}", p);
                        } catch (IOException e) {
                            log.warn("Failed to read file: {}", p, e);
                        }
                    });

            log.info("Successfully loaded {} code files from: {}", files.size(), repoPath);
        } catch (IOException e) {
            log.error("Failed to read repository: {}", repoPath, e);
            throw new RuntimeException("Failed to read repo", e);
        }

        return files;
    }

    private boolean isCodeFile(String file) {
        return file.endsWith(".java") ||
                file.endsWith(".js") ||
                file.endsWith(".ts") ||
                file.endsWith(".py") ||
                file.endsWith(".cpp") ||
                file.endsWith(".html") ||
                file.endsWith(".css");
    }
}