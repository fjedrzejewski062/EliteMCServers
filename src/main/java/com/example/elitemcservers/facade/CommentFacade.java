package com.example.elitemcservers.facade;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.repository.CommentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentFacade {

    private final CommentRepository commentRepository;

    public CommentFacade(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public List<Comment> findCommentsByServerId(Long serverId) {
        return commentRepository.findByServerId(serverId);
    }
}
