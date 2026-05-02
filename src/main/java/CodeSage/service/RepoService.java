package CodeSage.service;

import java.io.File;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RepoService {
    private static final Logger log = LoggerFactory.getLogger(RepoService.class);

    public String cloneRepo(String repoUrl) {
        log.info("Cloning repository from URL: {}", repoUrl);
        try {
            String localPath = "./repos/repo";
            File dir = new File(localPath);
            
            // Avoid re-cloning
            if (dir.exists()) {
                log.info("Repository already exists at: {}", dir.getAbsolutePath());
                return dir.getAbsolutePath();
            }
            
            log.debug("Starting clone operation to: {}", localPath);
            Git git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(dir)
                    .call();
            git.close();
            
            log.info("Repository cloned successfully to: {}", dir.getAbsolutePath());
            return dir.getAbsolutePath();
        } catch (Exception e) {
            log.error("Failed to clone repository from URL: {}", repoUrl, e);
            throw new RuntimeException("Failed to clone repo", e);
        }
    }
}
