package com.example.elitemcservers.entity;

import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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
}
