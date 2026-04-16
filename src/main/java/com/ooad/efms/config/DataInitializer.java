package com.ooad.efms.config;

import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ApprovingAuthority;
import com.ooad.efms.model.Organizer;
import com.ooad.efms.repository.ApprovingAuthorityRepository;
import com.ooad.efms.repository.OrganizerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return args -> {
            if (organizerRepository.count() == 0) {
                Organizer o = new Organizer("Demo Organizer", "demo@college.edu");
                o.setPasswordHash(encoder.encode("demo123"));
                organizerRepository.save(o);
            }
            if (authorityRepository.count() == 0) {
                ApprovingAuthority l1 = new ApprovingAuthority(
                        "Dr. Faculty Coord", "coord@college.edu",
                        ApprovalRole.L1, new BigDecimal("3000"));
                l1.setPasswordHash(encoder.encode("coord123"));
                authorityRepository.save(l1);
                ApprovingAuthority l2 = new ApprovingAuthority(
                        "Prof. Finance Chair", "finance@college.edu",
                        ApprovalRole.L2, new BigDecimal("50000"));
                l2.setPasswordHash(encoder.encode("finance123"));
                authorityRepository.save(l2);
            }
        };
    }
}
