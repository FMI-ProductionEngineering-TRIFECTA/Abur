package ro.unibuc.hello;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ro.unibuc.hello.config.DatabaseSeeder;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "ro.unibuc.hello.data")
public class HelloApplication {

    @Autowired
    private DatabaseSeeder databaseSeeder;

	public static void main(String[] args) {
		SpringApplication.run(HelloApplication.class, args);
	}

	@PostConstruct
	public void runAfterObjectCreated() {
		databaseSeeder.seedData();
	}

}
