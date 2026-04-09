package com.ooad.efms.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ORGANIZER")
public class Organizer extends User {

    public Organizer() { super(); }

    public Organizer(String name, String email) {
        super(name, email);
    }

    @Override
    public String getRoleName() {
        return "ORGANIZER";
    }
}
