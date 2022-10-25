package poc;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JavaK6Application {

	public static void main(String[] args) {
		SpringApplication.run(JavaK6Application.class, args);
		System.out.println("##### SERVICE STARTED #####");
	}
}