package com.ooad.efms.config;

import com.ooad.efms.model.Organizer;
import com.ooad.efms.repository.OrganizerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds a demo organizer on startup so Postman flows can hit the API
 * immediately without having to create a user first.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(OrganizerRepository organizerRepository) {
        return args -> {
            if (organizerRepository.count() == 0) {
                organizerRepository.save(new Organizer("Demo Organizer", "demo@college.edu"));
            }
        };
    }
}
