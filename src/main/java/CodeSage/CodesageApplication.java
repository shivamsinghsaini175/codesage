package CodeSage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodesageApplication {

	private static final Logger log = LoggerFactory.getLogger(CodesageApplication.class);

	public static void main(String[] args) {
		log.info("Starting CodeSage Application...");
		SpringApplication.run(CodesageApplication.class, args);
		log.info("CodeSage Application started successfully!");
	}

}
