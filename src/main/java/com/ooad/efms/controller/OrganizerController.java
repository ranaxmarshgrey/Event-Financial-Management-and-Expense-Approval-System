package com.ooad.efms.controller;

import com.ooad.efms.model.Organizer;
import com.ooad.efms.repository.OrganizerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Minimal organizer endpoint so Postman can seed/list organizers.
 * Full user management belongs to the teammate handling authentication.
 */
@RestController
@RequestMapping("/api/organizers")
public class OrganizerController {

    private final OrganizerRepository organizerRepository;

    public OrganizerController(OrganizerRepository organizerRepository) {
        this.organizerRepository = organizerRepository;
    }

    @PostMapping
    public ResponseEntity<Organizer> create(@RequestBody Map<String, String> body) {
        Organizer o = new Organizer(body.get("name"), body.get("email"));
        return ResponseEntity.ok(organizerRepository.save(o));
    }

    @GetMapping
    public ResponseEntity<List<Organizer>> list() {
        return ResponseEntity.ok(organizerRepository.findAll());
    }
}
