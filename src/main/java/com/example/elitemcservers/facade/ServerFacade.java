package com.example.elitemcservers.facade;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.ServerVote;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.service.CommentService;
import com.example.elitemcservers.service.ServerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class ServerFacade {

    private final ServerService serverService;
    private final CommentService commentService;

    public ServerFacade(ServerService serverService, CommentService commentService) {
        this.serverService = serverService;
        this.commentService = commentService;
    }

    public void createServer(Server server, User user) {
        serverService.createServer(server, user);
    }
    public void voteServer(Server server, String voteType, User user) {
        Optional<ServerVote> existingVoteOpt = serverService.findVoteByUserAndServer(user, server);

        if (existingVoteOpt.isPresent()) {
            ServerVote existingVote = existingVoteOpt.get();
            String currentVote = existingVote.getVoteType();

            if (!currentVote.equalsIgnoreCase(voteType)) {
                if ("UP".equalsIgnoreCase(currentVote)) {
                    server.setUpVotes(server.getUpVotes() - 1);
                    server.setDownVotes(server.getDownVotes() + 1);
                } else {
                    server.setDownVotes(server.getDownVotes() - 1);
                    server.setUpVotes(server.getUpVotes() + 1);
                }

                existingVote.setVoteType(voteType.toUpperCase());
                serverService.saveVote(existingVote);
            }
        } else {
            ServerVote vote = new ServerVote();
            vote.setUser(user);
            vote.setServer(server);
            vote.setVoteType(voteType.toUpperCase());

            if ("UP".equalsIgnoreCase(voteType)) {
                serverService.upVote(server);
            } else {
                serverService.downVote(server);
            }

            serverService.saveVote(vote);
            return;
        }

        server.setScore(server.getUpVotes() - server.getDownVotes());
        serverService.saveServer(server);
    }

    public void addComment(Server server, Comment comment, User user) {
        comment.setId(null);
        comment.setServer(server);
        comment.setCreatedBy(user);
        comment.setCreationDate(LocalDateTime.now());
        commentService.save(comment);
    }

    public void updateServer(Server existingServer, Server updatedServer) {
        existingServer.setServerName(updatedServer.getServerName());
        existingServer.setIpAddress(updatedServer.getIpAddress());
        existingServer.setMode(updatedServer.getMode());
        existingServer.setVersion(updatedServer.getVersion());
        existingServer.setDescription(updatedServer.getDescription());
        serverService.updateServer(existingServer);
    }

    public Server findById(Long id) {
        return serverService.findById(id);
    }

    public void deleteServer(Long id) {
        serverService.deleteServer(id);
    }
    public Page<Server> findFilteredServers(User createdBy,
                                            String serverName,
                                            String ipAddress,
                                            ServerVersion version,
                                            ServerMode mode,
                                            Integer minScore,
                                            Integer maxScore,
                                            Pageable pageable) {
        return serverService.findFilteredServers(createdBy, serverName, ipAddress, version, mode, minScore, maxScore, pageable);
    }

    public Page<Server> findFilteredServersAdmin(User createdBy,
                                                 String serverName,
                                                 String ipAddress,
                                                 ServerVersion version,
                                                 ServerMode mode,
                                                 Integer minScore,
                                                 Integer maxScore,
                                                 ServerStatus status,
                                                 LocalDateTime createdAfter,
                                                 LocalDateTime createdBefore,
                                                 LocalDateTime updatedAfter,
                                                 LocalDateTime updatedBefore,
                                            Pageable pageable) {
        return serverService.findFilteredServersAdmin(createdBy, serverName, ipAddress, version, mode, minScore, maxScore, status, createdAfter, createdBefore, updatedAfter, updatedBefore, pageable);
    }

    public Page<Server> findFilteredServersForUser(Long createdBy,
                                                   String serverName,
                                                   String ipAddress,
                                                   ServerVersion version,
                                                   ServerMode mode,
                                                   Integer minScore,
                                                   Integer maxScore,
                                                   ServerStatus status,
                                                   LocalDateTime createdAfter,
                                                   LocalDateTime createdBefore,
                                                   LocalDateTime updatedAfter,
                                                   LocalDateTime updatedBefore,
                                                   Pageable pageable) {
        return serverService.findFilteredServersForUser(
                createdBy,
                serverName,
                ipAddress,
                version,
                mode,
                minScore,
                maxScore,
                status,
                createdAfter,
                createdBefore,
                updatedAfter,
                updatedBefore,
                pageable
        );
    }
}
