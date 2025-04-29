package com.example.elitemcservers.repository;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    List<Comment> findByServerId(Long serverId);
    List<Comment> findByCreatedBy(User createdBy);
    Page<Comment> findByCreatedBy(User createdBy, Pageable pageable);
}
