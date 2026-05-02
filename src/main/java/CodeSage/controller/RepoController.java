package CodeSage.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import CodeSage.service.RepoService;

@RestController
@RequestMapping("/repo")
public class RepoController {
    private static final Logger log = LoggerFactory.getLogger(RepoController.class);
    private final RepoService repoService;

    public RepoController(RepoService repoService) {
        this.repoService = repoService;
        log.info("RepoController initialized");
    }

    @PostMapping("/clone")
    public Map<String, String> clone(@RequestBody Map<String, String> req) {
        String url = req.get("url");
        log.info("Clone request received for URL: {}", url);
        try {
            String path = repoService.cloneRepo(url);
            log.info("Repository cloned successfully to: {}", path);
            return Map.of("path", path);
        } catch (Exception e) {
            log.error("Error cloning repository: {}", url, e);
            throw e;
        }
    }
}
