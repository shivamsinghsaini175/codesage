package CodeSage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final ChatClient chatClient;

    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        log.info("AiService initialized with ChatClient");
    }

    public String generate(String prompt) {
        log.debug("Generating response for prompt: {}", prompt);
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.info("Response generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error generating response for prompt: {}", prompt, e);
            throw e;
        }
    }
}
