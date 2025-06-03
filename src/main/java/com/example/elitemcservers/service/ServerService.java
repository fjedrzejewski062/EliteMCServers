package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.ServerVote;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.repository.ServerRepository;
import com.example.elitemcservers.repository.ServerVoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServerService {
    private final ServerRepository serverRepository;
    private final ServerVoteRepository serverVoteRepository;

    public ServerService(ServerRepository serverRepository, ServerVoteRepository serverVoteRepository) {
        this.serverRepository = serverRepository;
        this.serverVoteRepository = serverVoteRepository;
    }

    public Optional<Server> findByServerName(String serverName){
        return serverRepository.findByServerName(serverName);
    }

    List<Server> findByCreatedBy(User createdBy){
        return serverRepository.findByCreatedBy(createdBy);
    }

    public Server createServer(Server server, User createdBy){
        server.setCreatedBy(createdBy);
        server.setStatus(ServerStatus.PENDING);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    public Server updateServer(Server server){
        server.setUpdatedAt(LocalDateTime.now());
        return serverRepository.save(server);
    }

    public void deleteServer(Long id){
        serverRepository.deleteById(id);
    }

    public void upVote(Server server) {
        server.setUpVotes(server.getUpVotes() + 1);
        server.setScore(server.getUpVotes() - server.getDownVotes());
        serverRepository.save(server);
    }

    public void downVote(Server server) {
        server.setDownVotes(server.getDownVotes() + 1);
        server.setScore(server.getUpVotes() - server.getDownVotes());
        serverRepository.save(server);
    }
    public Optional<ServerVote> findVoteByUserAndServer(User user, Server server) {
        return serverVoteRepository.findByUserAndServer(user, server);
    }

    public void saveVote(ServerVote vote) {
        serverVoteRepository.save(vote);
    }

    public void saveServer(Server server) {
        serverRepository.save(server);
    }
    public Server findById(Long id){
        return serverRepository.findById(id).orElse(null);
    }

    public Page<Server> findFilteredServers(User createdBy,
                                            String serverName,
                                            String ipAddress,
                                            ServerVersion version,
                                            ServerMode mode,
                                            Integer minScore,
                                            Integer maxScore,
                                            Pageable pageable) {

        Specification<Server> spec = Specification.where(null);

        if (serverName != null && !serverName.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("serverName")), "%" + serverName.toLowerCase() + "%"));
        }

        if (ipAddress != null && !ipAddress.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("ipAddress")), "%" + ipAddress.toLowerCase() + "%"));
        }

        if (version != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("version"), version));
        }

        if (mode != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("mode"), mode));
        }

        if (minScore != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("score"), minScore));
        }
        if (maxScore != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("score"), maxScore));
        }

        spec = spec.and((root, query, builder) -> builder.equal(root.get("status"), ServerStatus.APPROVED));

        return serverRepository.findAll(spec, pageable);
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

        Specification<Server> spec = Specification.where(null);

        if (serverName != null && !serverName.isEmpty()) {
            spec = spec.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("serverName")), "%" + serverName.toLowerCase() + "%"));
        }

        if (ipAddress != null && !ipAddress.isEmpty()) {
            spec = spec.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("ipAddress")), "%" + ipAddress.toLowerCase() + "%"));
        }

        if (version != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("version"), version));
        }

        if (mode != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("mode"), mode));
        }

        if (minScore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("score"), minScore));
        }

        if (maxScore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("score"), maxScore));
        }

        if (status != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }

        if (createdAfter != null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
        }

        if (createdBefore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
        }

        if (updatedAfter != null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("updatedAt"), updatedAfter));
        }

        if (updatedBefore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("updatedAt"), updatedBefore));
        }

        if (createdBy != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("createdBy"), createdBy));
        }

        return serverRepository.findAll(spec, pageable);
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

        Specification<Server> spec = Specification.where(null);

        if (createdBy != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("createdBy").get("id"), createdBy));
        }

        if (serverName != null && !serverName.isEmpty()) {
            spec = spec.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("serverName")), "%" + serverName.toLowerCase() + "%"));
        }

        if (ipAddress != null && !ipAddress.isEmpty()) {
            spec = spec.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("ipAddress")), "%" + ipAddress.toLowerCase() + "%"));
        }

        if (version != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("version"), version));
        }

        if (mode != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("mode"), mode));
        }

        if (minScore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("score"), minScore));
        }

        if (maxScore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("score"), maxScore));
        }

        if (status != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }

        if (createdAfter != null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
        }

        if (createdBefore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
        }

        if (updatedAfter != null) {
            spec = spec.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("updatedAt"), updatedAfter));
        }

        if (updatedBefore != null) {
            spec = spec.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("updatedAt"), updatedBefore));
        }

        return serverRepository.findAll(spec, pageable);
    }



}
