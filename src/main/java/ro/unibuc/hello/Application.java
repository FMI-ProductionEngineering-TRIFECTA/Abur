package ro.unibuc.hello;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ro.unibuc.hello.config.DatabaseSeeder;

@SpringBootApplication
@EnableAspectJAutoProxy
@EntityScan(basePackages = "ro.unibuc.hello.data.entity")
@EnableMongoRepositories(basePackages = "ro.unibuc.hello.data")
public class Application {

    @Autowired
    private DatabaseSeeder databaseSeeder;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@PostConstruct
	public void runAfterObjectCreated() {
		databaseSeeder.seedData();
	}

}
