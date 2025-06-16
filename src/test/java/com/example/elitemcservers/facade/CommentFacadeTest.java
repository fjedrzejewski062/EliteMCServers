package com.example.elitemcservers.facade;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentFacadeTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentFacade commentFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("save calls repository save and returns saved comment")
    void save_Success() {
        Comment comment = new Comment();
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment saved = commentFacade.save(comment);

        assertEquals(comment, saved);
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("findById returns comment when found")
    void findById_Found() {
        Comment comment = new Comment();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Optional<Comment> result = commentFacade.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(comment, result.get());
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("findById returns empty when not found")
    void findById_NotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Comment> result = commentFacade.findById(1L);

        assertFalse(result.isPresent());
        verify(commentRepository).findById(1L);
    }

    @Test
    @DisplayName("deleteComment calls repository deleteById")
    void deleteComment_CallsRepository() {
        commentFacade.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("findCommentsByServerId returns list of comments")
    void findCommentsByServerId_ReturnsComments() {
        Comment c1 = new Comment();
        Comment c2 = new Comment();
        List<Comment> comments = Arrays.asList(c1, c2);

        when(commentRepository.findByServerId(10L)).thenReturn(comments);

        List<Comment> result = commentFacade.findCommentsByServerId(10L);

        assertEquals(2, result.size());
        assertEquals(comments, result);
        verify(commentRepository).findByServerId(10L);
    }
}
