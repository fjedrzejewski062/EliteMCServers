package com.example.elitemcservers.facade;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.ServerVote;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.service.CommentService;
import com.example.elitemcservers.service.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerFacadeTest {

    @Mock
    private ServerService serverService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ServerFacade serverFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("createServer calls service createServer")
    void createServer_CallsService() {
        Server server = new Server();
        User user = new User();

        serverFacade.createServer(server, user);

        verify(serverService).createServer(server, user);
    }

    @Test
    @DisplayName("voteServer - new UP vote increments upVotes and saves vote")
    void voteServer_NewUpVote() {
        Server server = new Server();
        server.setUpVotes(5);
        server.setDownVotes(2);
        User user = new User();

        when(serverService.findVoteByUserAndServer(user, server)).thenReturn(Optional.empty());

        serverFacade.voteServer(server, "UP", user);

        verify(serverService).upVote(server);
        verify(serverService).saveVote(any(ServerVote.class));
        // Since new vote, score not recalculated here
        verify(serverService, never()).saveServer(server);
    }

    @Test
    @DisplayName("voteServer - changes vote from UP to DOWN updates counts and saves")
    void voteServer_ChangeVoteUpToDown() {
        Server server = new Server();
        server.setUpVotes(10);
        server.setDownVotes(3);
        User user = new User();

        ServerVote existingVote = new ServerVote();
        existingVote.setVoteType("UP");

        when(serverService.findVoteByUserAndServer(user, server)).thenReturn(Optional.of(existingVote));

        serverFacade.voteServer(server, "DOWN", user);

        assertEquals(9, server.getUpVotes());
        assertEquals(4, server.getDownVotes());

        verify(serverService).saveVote(existingVote);
        verify(serverService).saveServer(server);
    }

    @Test
    @DisplayName("addComment sets properties and saves comment")
    void addComment_SetsPropertiesAndSaves() {
        Server server = new Server();
        Comment comment = new Comment();
        User user = new User();

        serverFacade.addComment(server, comment, user);

        assertNull(comment.getId());
        assertEquals(server, comment.getServer());
        assertEquals(user, comment.getCreatedBy());
        assertNotNull(comment.getCreationDate());

        verify(commentService).save(comment);
    }

    @Test
    @DisplayName("updateServer updates existingServer properties and calls update")
    void updateServer_UpdatesProperties() {
        Server existing = new Server();
        existing.setServerName("OldName");
        existing.setIpAddress("1.1.1.1");
        existing.setMode(ServerMode.SURVIVAL);
        existing.setVersion(ServerVersion.V1_16_0);
        existing.setDescription("Old desc");

        Server updated = new Server();
        updated.setServerName("NewName");
        updated.setIpAddress("2.2.2.2");
        updated.setMode(ServerMode.CREATIVE);
        updated.setVersion(ServerVersion.V1_17_0);
        updated.setDescription("New desc");

        serverFacade.updateServer(existing, updated);

        assertEquals("NewName", existing.getServerName());
        assertEquals("2.2.2.2", existing.getIpAddress());
        assertEquals(ServerMode.CREATIVE, existing.getMode());
        assertEquals(ServerVersion.V1_17_0, existing.getVersion());
        assertEquals("New desc", existing.getDescription());

        verify(serverService).updateServer(existing);
    }

    @Test
    @DisplayName("findById delegates to serverService")
    void findById_Delegates() {
        Server server = new Server();
        when(serverService.findById(1L)).thenReturn(server);

        Server result = serverFacade.findById(1L);

        assertEquals(server, result);
    }

    @Test
    @DisplayName("deleteServer delegates to serverService")
    void deleteServer_Delegates() {
        serverFacade.deleteServer(1L);

        verify(serverService).deleteServer(1L);
    }

    @Test
    @DisplayName("findFilteredServers delegates to serverService")
    void findFilteredServers_Delegates() {
        Pageable pageable = mock(Pageable.class);
        Page<Server> page = new PageImpl<>(List.of());
        when(serverService.findFilteredServers(null, null, null, null, null, null, null, pageable)).thenReturn(page);

        Page<Server> result = serverFacade.findFilteredServers(null, null, null, null, null, null, null, pageable);

        assertEquals(page, result);
    }
}
