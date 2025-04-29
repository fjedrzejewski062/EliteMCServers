package com.example.elitemcservers.entity;

import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "servers")
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String serverName;

    @Column(nullable = true)
    private String ipAddress;


    // Wersja - pole enum (od jakiej do jakiej)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerVersion version;

    // Tryb serwera (np. Survival, Creative itp.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerMode mode;

    private int upVotes = 0;
    private int downVotes = 0;
    private int score = 0;

    // Data dodania serwera
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Data ostatniej aktualizacji serwera
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
    private List<Server> servers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ServerVersion getVersion() {
        return version;
    }

    public void setVersion(ServerVersion version) {
        this.version = version;
    }

    public ServerMode getMode() {
        return mode;
    }

    public void setMode(ServerMode mode) {
        this.mode = mode;
    }

    public int getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(int upVotes) {
        this.upVotes = upVotes;
    }

    public int getDownVotes() {
        return downVotes;
    }

    public void setDownVotes(int downVotes) {
        this.downVotes = downVotes;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }
}
