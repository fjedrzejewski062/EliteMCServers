package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.facade.ServerFacade;
import com.example.elitemcservers.facade.UserFacade;
import com.example.elitemcservers.service.CommentService;
import com.example.elitemcservers.service.ServerService;
import com.example.elitemcservers.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/servers")
public class ServerController {
    private final ServerFacade serverFacade;
    private final UserFacade userFacade;
    private final CommentService commentService;

    public ServerController(ServerFacade serverFacade,
                            UserFacade userFacade,
                            CommentService commentService) {
        this.serverFacade = serverFacade;
        this.userFacade = userFacade;
        this.commentService = commentService;
    }

    @GetMapping("/create")
    public String showCreateServerForm(Model model){
        model.addAttribute("server", new Server());
        return "createServer";
    }


    @PostMapping("/create")
    public String createServer(@Valid @ModelAttribute("server") Server server,
                             BindingResult result,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("server", server); // ← potrzebne przy błędach
            return "createServer"; // ← wróć do formularza z błędami
        }

        String email = currentUser.getUsername();
        User user = userFacade.findByEmail(email).orElse(null);

        serverFacade.createServer(server, user);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String serverDetail(@PathVariable Long id,
                               @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                               Model model) {
        Server server = serverFacade.findById(id);
        if (server == null) {
            return "redirect:/";
        }


        model.addAttribute("server", server);
        model.addAttribute("comment", new Comment());

        if (currentUser != null) {
            model.addAttribute("currentUserEmail", currentUser.getUsername());
        }

        return "serverDetail";
    }

    @PostMapping("/{id}/vote")
    public String voteServer(@PathVariable Long id,
                           @RequestParam("vote") String vote,
                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        if (currentUser == null) {
            return "redirect:/login"; // lub przekierowanie z komunikatem
        }

        Server server = serverFacade.findById(id);
        if (server != null) {
            Optional<User> userOptional = userFacade.findByEmail(currentUser.getUsername());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                serverFacade.voteServer(server, vote, user);
            }
        }
        return "redirect:/";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("comment") Comment newComment,
                             BindingResult result,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             Model model) {
        // Logika dodawania komentarza
        Server server = serverFacade.findById(id);
        if (server == null) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            model.addAttribute("server", server);
            model.addAttribute("comment", newComment);
            return "serverDetail";
        }

        if (currentUser == null) {
            return "redirect:/login";
        }

        User user = userFacade.findByEmail(currentUser.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Dodawanie komentarza
        newComment.setId(null); // ensure we are adding a new comment
        serverFacade.addComment(server, newComment, user);
        commentService.save(newComment);

        return "redirect:/servers/" + id;
    }

    @GetMapping("/{id}/comments/{commentId}/edit")
    public String editCommentForm(@PathVariable Long commentId,
                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                  Model model) {
        Optional<Comment> optionalComment = commentService.findById(commentId);
        if (optionalComment.isEmpty()) {
            return "redirect:/";
        }

        Comment comment = optionalComment.get();
        if (!comment.getCreatedBy().getEmail().equals(currentUser.getUsername())) {
            return "redirect:/";
        }

        // Przekazujemy id komentarza, który edytujemy
        model.addAttribute("editingCommentId", commentId);
        model.addAttribute("server", comment.getServer());
        model.addAttribute("comment", comment); // Formularz edytowania komentarza

        return "editComment"; // Z powrotem do serverDetail z formularzem edycji
    }

    @PostMapping("/{id}/comments/{commentId}/edit")
    public String editCommentSubmit(@PathVariable Long commentId,
                                    @Valid @ModelAttribute("comment") Comment updatedComment,
                                    BindingResult result,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                    Model model) {
        Optional<Comment> optionalComment = commentService.findById(commentId);
        if (optionalComment.isEmpty()) {
            return "redirect:/";
        }

        Comment comment = optionalComment.get();
        if (!comment.getCreatedBy().getEmail().equals(currentUser.getUsername())) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            model.addAttribute("comment", updatedComment);
            return "serverDetail"; // pozostań na tej samej stronie
        }

        // Zaktualizowanie komentarza w bazie
        comment.setContent(updatedComment.getContent());
        commentService.save(comment);

        return "redirect:/servers/" + comment.getServer().getId();
    }


    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Optional<Comment> optionalComment = commentService.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            if (comment.getCreatedBy().getEmail().equals(currentUser.getUsername())) {
                Long serverId = comment.getServer().getId();
                commentService.deleteComment(commentId);
                return "redirect:/servers/" + serverId;
            }
        }
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditServer(@PathVariable Long id, Model model,
                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                                 RedirectAttributes redirectAttributes){
        Server server = serverFacade.findById(id);

        if(server == null || !server.getCreatedBy().getEmail().equals(currentUser.getUsername())){
            redirectAttributes.addFlashAttribute("error", "This is not your server!");
            return "redirect:/servers/" + id;
        }

        model.addAttribute("server", server);
        return "editServer";
    }

    @PostMapping("/edit/{id}")
    public String editServer(@PathVariable Long id,
                             @Valid @ModelAttribute("server") Server updatedServer,
                             BindingResult result,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             Model model,
                             RedirectAttributes redirectAttributes){
        Server existingServer = serverFacade.findById(id);

        if(existingServer == null || !existingServer.getCreatedBy().getEmail().equals(currentUser.getUsername())){
            redirectAttributes.addFlashAttribute("error", "This is not your server!");
            return "redirect:/servers/" + id;
        }

        if (result.hasErrors()) {
            model.addAttribute("server", updatedServer); // ← ważne!
            return "editServer"; // ← wracamy do formularza z błędami
        }

        serverFacade.updateServer(existingServer, updatedServer);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteServer(@PathVariable Long id,
                               @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                               RedirectAttributes redirectAttributes){
        Server server = serverFacade.findById(id);
        if(server != null && server.getCreatedBy().getEmail().equals(currentUser.getUsername())){
            serverFacade.deleteServer(id);
        }
        redirectAttributes.addFlashAttribute("error", "This is not your server!");
        return "redirect:/servers/" + id;
    }
}
