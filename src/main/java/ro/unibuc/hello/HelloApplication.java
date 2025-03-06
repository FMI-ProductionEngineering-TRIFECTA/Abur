package ro.unibuc.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import ro.unibuc.hello.data.*;

import jakarta.annotation.PostConstruct;
import ro.unibuc.hello.service.AuthenticationService;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "ro.unibuc.hello.data")
public class HelloApplication {

	@Autowired
	private InformationRepository informationRepository;

	@Autowired
	private DeveloperRepository developerRepository;

	@Autowired
	private CustomerRepository customerRepository;

	public static void main(String[] args) {
		SpringApplication.run(HelloApplication.class, args);
	}

	@PostConstruct
	public void runAfterObjectCreated() {
		informationRepository.deleteAll();
		informationRepository.save(new InformationEntity(
				"Overview",
				"This is an example of using a data storage engine running separately from our applications server"
		));

		developerRepository.deleteAll();
		developerRepository.save(new DeveloperEntity(
				"67c9f02a5582625f6c6639b4",
				"PlayStationStudios",
				AuthenticationService.encryptPassword("PlayStationStudios1234"),
				"contact@sony.com",
				"PlayStation Studios",
				"https://www.playstation.com/playstation-studios/"
		));

		customerRepository.deleteAll();
		customerRepository.save(new CustomerEntity(
				"67c9f02a5582625f6c6639b5",
				"FixBambucea",
				AuthenticationService.encryptPassword("FixBambucea1234"),
				"fixbambucea@gmail.com",
				"Bambucea",
				"Fix"
		));
	}

}
