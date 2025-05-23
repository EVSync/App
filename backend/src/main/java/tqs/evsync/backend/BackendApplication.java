package tqs.evsync.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "tqs.evsync.backend.repository")
@EntityScan(basePackages = "tqs.evsync.backend.model")
@ComponentScan(basePackages = {
				"tqs.evsync.backend",
				"tqs.evsync.backend.userStorie1",
})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
