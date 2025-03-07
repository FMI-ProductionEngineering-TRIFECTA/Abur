package ro.unibuc.hello.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.unibuc.hello.data.entity.CustomerEntity;
import ro.unibuc.hello.data.entity.DeveloperEntity;
import ro.unibuc.hello.data.entity.InformationEntity;
import ro.unibuc.hello.data.repository.CustomerRepository;
import ro.unibuc.hello.data.repository.DeveloperRepository;
import ro.unibuc.hello.data.repository.InformationRepository;
import ro.unibuc.hello.security.AuthenticationService;

@Component
public class DatabaseSeeder {
    @Autowired
    private InformationRepository informationRepository;

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private void seedInformation() {
        informationRepository.deleteAll();
        informationRepository.save(new InformationEntity(
                "Overview",
                "This is an example of using a data storage engine running separately from our applications server"
        ));
    }

    private void seedDeveloper() {
        developerRepository.deleteAll();
        developerRepository.save(new DeveloperEntity(
                "67c9f02a5582625f6c6639b4",
                "PlayStationStudios",
                AuthenticationService.encryptPassword("PlayStationStudios1234"),
                "contact@sony.com",
                "PlayStation Studios",
                "https://www.playstation.com/playstation-studios/"
        ));
    }

    private void seedCustomer() {
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

    @PostConstruct
    public void seedData() {
        seedInformation();
        seedDeveloper();
        seedCustomer();
    }
}
