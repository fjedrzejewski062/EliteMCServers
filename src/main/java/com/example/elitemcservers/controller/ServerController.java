package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.facade.CommentFacade;
import com.example.elitemcservers.facade.ServerFacade;
import com.example.elitemcservers.facade.UserFacade;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/servers")
public class ServerController {
    private final ServerFacade serverFacade;
    private final UserFacade userFacade;
    private final CommentFacade commentFacade;

    public ServerController(ServerFacade serverFacade, UserFacade userFacade, CommentFacade commentFacade) {
        this.serverFacade = serverFacade;
        this.userFacade = userFacade;
        this.commentFacade = commentFacade;
    }

    @GetMapping("/create")
    public String showCreateServerForm(Model model){
        model.addAttribute("server", new Server());
        return "createServer";
    }


    @PostMapping("/create")
    public String createServer(@Valid @ModelAttribute("server") Server server,
                               BindingResult result,
                               Authentication authentication,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("server", server);
            return "createServer";
        }

        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        User user = userOpt.get();
        serverFacade.createServer(server, user);
        return "createServer_success";
    }


    @GetMapping("/{id}")
    public String serverDetail(@PathVariable Long id,
                               Authentication authentication,
                               Model model) {
        Server server = serverFacade.findById(id);
        if (server == null) {
            return "redirect:/";
        }

        model.addAttribute("server", server);
        model.addAttribute("comment", new Comment());

        userFacade.getAuthenticatedUser(authentication)
                .ifPresent(user -> model.addAttribute("currentUserEmail", user.getEmail()));

        return "serverDetail";
    }

    @PostMapping("/{id}/vote")
    public String voteServer(@PathVariable Long id,
                             @RequestParam("vote") String vote,
                             Authentication authentication) {
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        Server server = serverFacade.findById(id);
        if (server != null) {
            serverFacade.voteServer(server, vote, userOpt.get());
        }

        return "redirect:/";
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("comment") Comment newComment,
                             BindingResult result,
                             Authentication authentication,
                             Model model) {
        Server server = serverFacade.findById(id);
        if (server == null) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            model.addAttribute("server", server);
            model.addAttribute("comment", newComment);
            return "serverDetail";
        }

        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        newComment.setId(null);
        serverFacade.addComment(server, newComment, userOpt.get());
        commentFacade.save(newComment);

        return "redirect:/servers/" + id;
    }

    @GetMapping("/{id}/comments/{commentId}/edit")
    public String editCommentForm(@PathVariable Long commentId,
                                  Authentication authentication,
                                  Model model) {
        Optional<Comment> optionalComment = commentFacade.findById(commentId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (optionalComment.isEmpty() || userOpt.isEmpty()) {
            return "redirect:/";
        }

        Comment comment = optionalComment.get();
        if (!comment.getCreatedBy().getEmail().equals(userOpt.get().getEmail())) {
            return "redirect:/";
        }

        model.addAttribute("editingCommentId", commentId);
        model.addAttribute("server", comment.getServer());
        model.addAttribute("comment", comment);

        return "editComment";
    }

    @PostMapping("/{id}/comments/{commentId}/edit")
    public String editCommentSubmit(@PathVariable Long commentId,
                                    @Valid @ModelAttribute("comment") Comment updatedComment,
                                    BindingResult result,
                                    Authentication authentication,
                                    Model model) {
        Optional<Comment> optionalComment = commentFacade.findById(commentId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (optionalComment.isEmpty() || userOpt.isEmpty()) {
            return "redirect:/";
        }

        Comment comment = optionalComment.get();
        if (!comment.getCreatedBy().getEmail().equals(userOpt.get().getEmail())) {
            return "redirect:/";
        }

        if (result.hasErrors()) {
            model.addAttribute("comment", updatedComment);
            return "serverDetail";
        }

        comment.setContent(updatedComment.getContent());
        commentFacade.save(comment);

        return "redirect:/servers/" + comment.getServer().getId();
    }

    @PostMapping("/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                Authentication authentication) {
        Optional<Comment> optionalComment = commentFacade.findById(commentId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (optionalComment.isPresent() && userOpt.isPresent()) {
            Comment comment = optionalComment.get();
            if (comment.getCreatedBy().getEmail().equals(userOpt.get().getEmail())) {
                Long serverId = comment.getServer().getId();
                commentFacade.deleteComment(commentId);
                return "redirect:/servers/" + serverId;
            }
        }

        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditServer(@PathVariable Long id,
                                 Model model,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Server server = serverFacade.findById(id);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (server == null || userOpt.isEmpty() ||
                !server.getCreatedBy().getEmail().equals(userOpt.get().getEmail())) {
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
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Server existingServer = serverFacade.findById(id);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (existingServer == null || userOpt.isEmpty() ||
                !existingServer.getCreatedBy().getEmail().equals(userOpt.get().getEmail())) {
            redirectAttributes.addFlashAttribute("error", "This is not your server!");
            return "redirect:/servers/" + id;
        }

        if (result.hasErrors()) {
            model.addAttribute("server", updatedServer);
            return "editServer";
        }

        serverFacade.updateServer(existingServer, updatedServer);
        return "redirect:/servers/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteServer(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Server server = serverFacade.findById(id);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (server != null && userOpt.isPresent() &&
                server.getCreatedBy().getEmail().equals(userOpt.get().getEmail())) {
            serverFacade.deleteServer(id);
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("error", "This is not your server!");
        return "redirect:/servers/" + id;
    }
}