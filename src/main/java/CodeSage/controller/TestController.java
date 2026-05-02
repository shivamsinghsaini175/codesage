package CodeSage.controller;

import CodeSage.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    private final AiService aiService;

    public TestController(AiService aiService) {
        this.aiService = aiService;
        log.info("TestController initialized");
    }

    @GetMapping
    public String test() {
        log.info("TEST endpoint called");
        try {
            String response = aiService.generate("Say hello");
            log.info("Test response generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Error in test endpoint", e);
            throw e;
        }
    }
}