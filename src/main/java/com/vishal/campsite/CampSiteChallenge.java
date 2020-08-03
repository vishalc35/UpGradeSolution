package com.vishal.campsite;

import com.vishal.campsite.model.Campsite;
import com.vishal.campsite.repository.CampsiteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(scanBasePackages = {"com.vishal.campsite"})
public class CampSiteChallenge {

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "true");
        SpringApplication.run(CampSiteChallenge.class, args);
    }

    @Bean
    public CommandLineRunner demo(CampsiteRepository campsiteRepository) {
        return (args) -> {
            // Create the campsite
            Campsite campsite = new Campsite();
            campsite.setName("Volcano Island Raskita");
            campsiteRepository.save(campsite);

            Campsite campsite2 = new Campsite();
            campsite2.setName("Passover Islands");
            campsiteRepository.save(campsite2);
        };
    }
}
