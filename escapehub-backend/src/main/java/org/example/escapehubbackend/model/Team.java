package org.example.escapehubbackend.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Long creatorId; // Ο δημιουργός της ομάδας

    @ManyToMany//Πολλές ομάδες ανά χρήστη η πολλοί χρήστες σε μία ομάδα
    @JoinTable(//Δημιουργία ενδιάμεσου πίνακα με συσχετίσης
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members=new HashSet<>();

    public Team() {
    }

    public Team(String name, Long creatorId) {
        this.name = name;
        this.creatorId = creatorId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public Set<User> getMembers(){return members;}
    public void setMembers(Set<User> members){this.members=members;}
}
