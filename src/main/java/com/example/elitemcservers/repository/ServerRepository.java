package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ServerRepository extends JpaRepository<Server, Long>, JpaSpecificationExecutor<Server> {
    Optional<Server> findByServerName(String serverName);
    List<Server> findByCreatedBy(User createdBy);
    Page<Server> findByCreatedBy(User createdBy, Pageable pageable);
}
