package com.example.elitemcservers.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "server_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "server_id"})
})
public class ServerVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Server server;

    // "UP" lub "DOWN"
    @Column(nullable = false)
    private String voteType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getVoteType() {
        return voteType;
    }

    public void setVoteType(String voteType) {
        this.voteType = voteType;
    }
}
