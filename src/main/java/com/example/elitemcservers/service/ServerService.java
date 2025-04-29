package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.repository.ServerRepository;
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

    public ServerService(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    public Optional<Server> findByServerName(String serverName){
        return serverRepository.findByServerName(serverName);
    }

    List<Server> findByCreatedBy(User createdBy){
        return serverRepository.findByCreatedBy(createdBy);
    }

    public Server createServer(Server server, User createdBy){
        server.setCreatedBy(createdBy);
        return serverRepository.save(server);
    }

    public Server updateServer(Server server){
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
    public Server findById(Long id){
        return serverRepository.findById(id).orElse(null);
    }

    public Page<Server> findFilteredServers(User createdBy,
                                            String serverName,
                                            String ipAddress,
                                            ServerVersion version,
                                            ServerMode mode,
                                            Integer minUpVotes,
                                            Integer maxUpVotes,
                                            Integer minDownVotes,
                                            Integer maxDownVotes,
                                            Integer minScore,
                                            Integer maxScore,
                                            String startDate,
                                            String endDate,
                                            Pageable pageable) {

        Specification<Server> spec = Specification.where(null);

        // Walidacja głosów
        if (minUpVotes != null && maxUpVotes != null) {
            // Jeśli minUpVotes > maxUpVotes, rzucamy wyjątek
            if (minUpVotes > maxUpVotes) {
                throw new IllegalArgumentException("Minimum upVotes cannot be greater than maximum upVotes.");
            }
            spec = spec.and((root, query, builder) -> builder.between(root.get("upVotes"), minUpVotes, maxUpVotes));
        } else if (minUpVotes != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("upVotes"), minUpVotes));
        } else if (maxUpVotes != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("upVotes"), maxUpVotes));
        }

        if (minDownVotes != null && maxDownVotes != null) {
            // Jeśli minDownVotes > maxDownVotes, rzucamy wyjątek
            if (minDownVotes > maxDownVotes) {
                throw new IllegalArgumentException("Minimum downVotes cannot be greater than maximum downVotes.");
            }
            spec = spec.and((root, query, builder) -> builder.between(root.get("downVotes"), minDownVotes, maxDownVotes));
        } else if (minDownVotes != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("downVotes"), minDownVotes));
        } else if (maxDownVotes != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("downVotes"), maxDownVotes));
        }

        if (minScore != null && maxScore != null) {
            // Jeśli minScore > maxScore, rzucamy wyjątek
            if (minScore > maxScore) {
                throw new IllegalArgumentException("Minimum score cannot be greater than maximum score.");
            }
            spec = spec.and((root, query, builder) -> builder.between(root.get("score"), minScore, maxScore));
        } else if (minScore != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("score"), minScore));
        } else if (maxScore != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("score"), maxScore));
        }

        // Walidacja dat
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                if (end.isBefore(start)) {
                    throw new IllegalArgumentException("End date cannot be earlier than start date.");
                }
                spec = spec.and((root, query, builder) -> builder.between(root.get("createdAt"), start, end));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date format. Please use the format 'YYYY-MM-DD'.");
            }
        }

        // Inne warunki
        if (createdBy != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("createdBy"), createdBy));
        }

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

        return serverRepository.findAll(spec, pageable);
    }

}
