package CodeSage.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiConfiguration.class);

    @Bean
    @Primary
    public ChatModel chatModel(@Autowired(required = false) OpenAiChatModel openAiChatModel) {
        log.info("Configuring OpenAI ChatModel as primary");
        return openAiChatModel;
    }
}
