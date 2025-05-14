package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.ServerVote;
import com.example.elitemcservers.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerVoteRepository extends JpaRepository<ServerVote, Long> {
    Optional<ServerVote> findByUserAndServer(User user, Server server);
}
