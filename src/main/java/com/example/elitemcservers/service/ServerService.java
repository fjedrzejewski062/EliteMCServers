package com.example.elitemcservers.service;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.repository.ServerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

    public Page<Server> findByCreatedBy(User createdBy, String serverName, String serverMode,
                                        Pageable pageable) {

        Specification<Server> spec = Specification.where(null);

        if (createdBy != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("createdBy"), createdBy));
        }

        if (serverName != null && !serverName.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.like(builder.lower(root.get("serverName")), "%" + serverName.toLowerCase() + "%"));
        }

        if (serverMode != null && !serverMode.isEmpty()) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("mode"), serverMode));
        }

        return serverRepository.findAll(spec, pageable);
    }
}
