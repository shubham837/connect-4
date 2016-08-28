package gameplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer{

    public static ApplicationContext applicationContext;

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    public static void main(String args[]) {
        log.info("Starting Connect4 Game application");
        applicationContext = SpringApplication.run(Application.class, args);
    }

    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}