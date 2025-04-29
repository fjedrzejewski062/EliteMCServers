package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
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

@Controller
@RequestMapping("/servers")
public class ServerController {
    private final ServerService serverService;
    private final UserService userService;
    private final CommentService commentService;

    public ServerController(ServerService serverService, UserService userService, CommentService commentService) {
        this.serverService = serverService;
        this.userService = userService;
        this.commentService = commentService;
    }

//    @GetMapping("/myservers")
//    public String myServers(Model model,
//                            @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
//                            RedirectAttributes redirectAttributes,
//                            @RequestParam(required = false) String serverName,
//                            @RequestParam(required = false) String ipAddress,
//                            @RequestParam(required = false) ServerVersion version,
//                            @RequestParam(required = false) ServerMode mode,
//                            @RequestParam(required = false) Integer minUpVotes,
//                            @RequestParam(required = false) Integer maxUpVotes,
//                            @RequestParam(required = false) Integer minDownVotes,
//                            @RequestParam(required = false) Integer maxDownVotes,
//                            @RequestParam(required = false) Integer minScore,
//                            @RequestParam(required = false) Integer maxScore,
//                            @RequestParam(required = false) String startDate,
//                            @RequestParam(required = false) String endDate,
//                            @RequestParam(defaultValue = "0") int page,
//                            @RequestParam(defaultValue = "id") String sortField,
//                            @RequestParam(defaultValue = "asc") String sortDirection) {
//
//        // Pobranie użytkownika
//        String email = currentUser.getUsername();
//        User user = userService.findByEmail(email).orElse(null);
//
//        // Walidacja daty
//        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
//            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
//            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
//            if (end.isBefore(start)) {
//                redirectAttributes.addFlashAttribute("error", "End date cannot be earlier than start date.");
//                return "redirect:/myservers";
//            }
//        }
//
//        // Ustalenie sortowania
//        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
//        Pageable pageable = PageRequest.of(page, 10, sort);
//
//        // Wykonanie zapytania z filtrami
//        Page<Server> serverPage = serverService.findFilteredServers(
//                user, serverName, ipAddress, version, mode, minUpVotes, maxUpVotes,
//                minDownVotes, maxDownVotes, minScore, maxScore, startDate, endDate, pageable);
//
//        // Dodanie atrybutów do modelu
//        model.addAttribute("user", user);
//        model.addAttribute("servers", serverPage.getContent());
//        model.addAttribute("totalPages", serverPage.getTotalPages());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("sortField", sortField);
//        model.addAttribute("sortDirection", sortDirection);
//        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
//        model.addAttribute("serverName", serverName);
//        model.addAttribute("ipAddress", ipAddress);
//        model.addAttribute("version", version);
//        model.addAttribute("mode", mode);
//        model.addAttribute("minUpVotes", minUpVotes);
//        model.addAttribute("maxUpVotes", maxUpVotes);
//        model.addAttribute("minDownVotes", minDownVotes);
//        model.addAttribute("maxDownVotes", maxDownVotes);
//        model.addAttribute("minScore", minScore);
//        model.addAttribute("maxScore", maxScore);
//        model.addAttribute("startDate", startDate);
//        model.addAttribute("endDate", endDate);
//
//        return "myServers";  // Widok, który wyświetli listę serwerów
//    }
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
            return "createServer";
        }

        String email = currentUser.getUsername();
        User user = userService.findByEmail(email).orElse(null);

        serverService.createServer(server, user);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public String serverDetail(@PathVariable Long id, Model model) {
        Server server = serverService.findById(id);
        if (server == null) {
            return "redirect:/";
        }
        model.addAttribute("server", server);
        // Pusty obiekt komentarza, by w formularzu thymeleaf mieć th:object
        model.addAttribute("comment", new Comment());
        return "serverDetail";
    }

    @PostMapping("/{id}/vote")
    public String voteServer(@PathVariable Long id,
                           @RequestParam("vote") String vote,
                           @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Server server = serverService.findById(id);
        if (server == null) {
            return "redirect:/";
        }
        if ("up".equals(vote)) {
            serverService.upVote(server);
        } else if ("down".equals(vote)) {
            serverService.downVote(server);
        }
        return "redirect:/servers/" + id;
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @Valid @ModelAttribute("comment") Comment newComment,
                             BindingResult result,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             Model model) {
        Server server = serverService.findById(id);
        if (server == null) {
            return "redirect:/";
        }
        if (result.hasErrors()) {
            model.addAttribute("server", server);
            return "serverDetail";
        }
        if (currentUser == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(currentUser.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // DODAJEMY NOWY komentarz:
        // by mieć pewność, że id jest null:
        newComment.setId(null);

        newComment.setServer(server);
        newComment.setCreatedBy(user);
        newComment.setCreationDate(LocalDateTime.now());

        commentService.save(newComment);

        return "redirect:/servers/" + id;
    }


    @GetMapping("/edit/{id}")
    public String showEditServer(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser){
        Server server = serverService.findById(id);

        if(server == null || !server.getCreatedBy().getEmail().equals(currentUser.getUsername())){
            return "redirect:/";
        }

        model.addAttribute("server", server);
        return "editServer";
    }

    @PostMapping("/edit/{id}")
    public String editServer(@PathVariable Long id,
                             @Valid @ModelAttribute("server") Server updatedServer,
                             BindingResult result,
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             Model model){
        Server existingServer = serverService.findById(id);

        if(existingServer == null || !existingServer.getCreatedBy().getEmail().equals(currentUser.getUsername())){
            return "redirect:/";
        }

        existingServer.setServerName(updatedServer.getServerName());
        existingServer.setIpAddress(updatedServer.getIpAddress());
        existingServer.setMode(updatedServer.getMode());
        existingServer.setVersion(updatedServer.getVersion());
        serverService.updateServer(existingServer);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteServer(@PathVariable Long id,
                            @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser){
        Server server = serverService.findById(id);
        if(server != null && server.getCreatedBy().getEmail().equals(currentUser.getUsername())){
            serverService.deleteServer(id);
        }
        return "redirect:/";

    }

}
