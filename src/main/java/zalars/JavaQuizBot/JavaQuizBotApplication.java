package zalars.JavaQuizBot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaQuizBotApplication {

	private static final Logger log = LoggerFactory.getLogger(JavaQuizBotApplication.class);

	public static void main(String[] args) {

		log.info("=======> PROGRAM STARTED...");

		SpringApplication.run(JavaQuizBotApplication.class, args);
	}

}
