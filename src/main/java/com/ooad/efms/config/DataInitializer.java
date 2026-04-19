package com.ooad.efms.config;

import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ApprovingAuthority;
import com.ooad.efms.model.Organizer;
import com.ooad.efms.repository.ApprovingAuthorityRepository;
import com.ooad.efms.repository.OrganizerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Seeds demo users on startup so Postman / the UI flows have something to
 * work with without a setup step.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(OrganizerRepository organizerRepository,
                           ApprovingAuthorityRepository authorityRepository) {
        return args -> {
            if (organizerRepository.count() == 0) {
                organizerRepository.save(new Organizer("Demo Organizer", "demo@college.edu"));
            }
            if (authorityRepository.count() == 0) {
                authorityRepository.save(new ApprovingAuthority(
                        "Dr. Faculty Coord", "coord@college.edu",
                        ApprovalRole.L1, new BigDecimal("3000")));
                authorityRepository.save(new ApprovingAuthority(
                        "Prof. Finance Chair", "finance@college.edu",
                        ApprovalRole.L2, new BigDecimal("50000")));
            }
        };
    }
}
