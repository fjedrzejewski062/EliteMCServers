package com.example.elitemcservers.admin;

import com.example.elitemcservers.entity.Comment;
import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.entity.User;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerStatus;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.facade.CommentFacade;
import com.example.elitemcservers.facade.ServerFacade;
import com.example.elitemcservers.facade.UserFacade;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ServerFacade serverFacade;
    private final UserFacade userFacade;
    private final CommentFacade commentFacade;

    public AdminController(ServerFacade serverFacade, UserFacade userFacade, CommentFacade commentFacade) {
        this.serverFacade = serverFacade;
        this.userFacade = userFacade;
        this.commentFacade = commentFacade;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(){
        return "admin/adminDashboard.html";
    }

    @GetMapping("/servers")
    public String viewServer(@RequestParam(required = false) String serverName,
                             @RequestParam(required = false) String ipAddress,
                             @RequestParam(required = false) Integer versionId,
                             @RequestParam(required = false) Integer modeId,
                             @RequestParam(required = false) Integer minScore,
                             @RequestParam(required = false) Integer maxScore,
                             @RequestParam(required = false) Integer statusId,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(required = false) String updatedStartDate,
                             @RequestParam(required = false) String updatedEndDate,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "id") String sortField,
                             @RequestParam(defaultValue = "asc") String sortDirection,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (serverName != null && !serverName.isEmpty() && !serverName.matches("^[a-zA-Z0-9 .\\-]+$")) {
            redirectAttributes.addFlashAttribute("error", "Server name can only contain letters, numbers, spaces, dots, or hyphens.");
            return "redirect:/admin/servers";
        }

        if (ipAddress != null && !ipAddress.isEmpty() && !ipAddress.matches("^[a-zA-Z0-9 .\\-]{0,50}$")) {
            redirectAttributes.addFlashAttribute("error", "IP address or domain must be up to 50 characters and contain only letters, numbers, spaces, dots, or hyphens.");
            return "redirect:/admin/servers";
        }

        if ((minScore != null && minScore < 0) || (maxScore != null && maxScore < 0)) {
            redirectAttributes.addFlashAttribute("error", "Scores must be positive.");
            return "redirect:/admin/servers";
        }

        if (minScore != null && maxScore != null && minScore > maxScore) {
            redirectAttributes.addFlashAttribute("error", "Min score cannot be greater than max score.");
            return "redirect:/admin/servers";
        }

        LocalDateTime createdAfter = null;
        LocalDateTime createdBefore = null;
        LocalDateTime updatedAfter = null;
        LocalDateTime updatedBefore = null;

        try {
            if (startDate != null && !startDate.isEmpty()) {
                createdAfter = LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                createdBefore = LocalDateTime.parse(endDate);
            }
            if (createdAfter != null && createdBefore != null && createdBefore.isBefore(createdAfter)) {
                redirectAttributes.addFlashAttribute("error", "End date cannot be earlier than start date.");
                return "redirect:/admin/servers";
            }

            if (updatedStartDate != null && !updatedStartDate.isEmpty()) {
                updatedAfter = LocalDateTime.parse(updatedStartDate);
            }
            if (updatedEndDate != null && !updatedEndDate.isEmpty()) {
                updatedBefore = LocalDateTime.parse(updatedEndDate);
            }
            if (updatedAfter != null && updatedBefore != null && updatedBefore.isBefore(updatedAfter)) {
                redirectAttributes.addFlashAttribute("error", "Update end date cannot be earlier than start date.");
                return "redirect:/admin/servers";
            }
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use YYYY-MM-DDThh:mm.");
            return "redirect:/admin/servers";
        }

        if (page < 0) page = 0;

        List<String> allowedSortFields = List.of("id", "serverName", "ipAddress", "score");
        if (!allowedSortFields.contains(sortField)) sortField = "id";

        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            sortDirection = "asc";
        }

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        ServerVersion version = null;
        if (versionId != null) {
            ServerVersion[] versions = ServerVersion.values();
            if (versionId >= 0 && versionId < versions.length) version = versions[versionId];
        }

        ServerMode mode = null;
        if (modeId != null) {
            ServerMode[] modes = ServerMode.values();
            if (modeId >= 0 && modeId < modes.length) mode = modes[modeId];
        }

        ServerStatus status = null;
        if (statusId != null) {
            ServerStatus[] statuses = ServerStatus.values();
            if (statusId >= 0 && statusId < statuses.length) status = statuses[statusId];
        }

        Page<Server> serverPage = serverFacade.findFilteredServersAdmin(
                null,
                serverName,
                ipAddress,
                version,
                mode,
                minScore,
                maxScore,
                status,
                createdAfter,
                createdBefore,
                updatedAfter,
                updatedBefore,
                pageable
        );

        model.addAttribute("servers", serverPage.getContent());
        model.addAttribute("totalPages", serverPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

        model.addAttribute("serverName", serverName);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("versionId", versionId);
        model.addAttribute("modeId", modeId);
        model.addAttribute("statusId", statusId);
        model.addAttribute("minScore", minScore);
        model.addAttribute("maxScore", maxScore);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("updatedStartDate", updatedStartDate);
        model.addAttribute("updatedEndDate", updatedEndDate);
        model.addAttribute("versions", ServerVersion.values());
        model.addAttribute("modes", ServerMode.values());
        model.addAttribute("statuses", ServerStatus.values());

        return "admin/adminServerView";
    }



    @GetMapping("/servers/{id}")
    public String serverDetail(@PathVariable Long id,
                               Authentication authentication,
                               Model model) {
        Server server = serverFacade.findById(id);
        if (server == null) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("server", server);
        model.addAttribute("comment", new Comment());

        userFacade.getAuthenticatedUser(authentication)
                .ifPresent(user -> model.addAttribute("currentUserEmail", user.getEmail()));

        return "admin/adminServerDetail";
    }

    @GetMapping("/servers/edit/{id}")
    public String showEditServer(@PathVariable Long id,
                                 Model model,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        Server server = serverFacade.findById(id);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (server == null || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Server not found or not authenticated.");
            return "redirect:/admin/servers";
        }

        User currentUser = userOpt.get();
        boolean isOwner = server.getCreatedBy().getEmail().equals(currentUser.getEmail());
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this server!");
            return "redirect:/admin/servers/" + id;
        }

        model.addAttribute("server", server);
        return "admin/adminEditServer";
    }


    @PostMapping("/servers/edit/{id}")
    public String editServer(@PathVariable Long id,
                             @Valid @ModelAttribute("server") Server updatedServer,
                             BindingResult result,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Server existingServer = serverFacade.findById(id);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (existingServer == null || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Server not found or not authenticated.");
            return "redirect:/admin/servers";
        }

        User currentUser = userOpt.get();
        boolean isOwner = existingServer.getCreatedBy().getEmail().equals(currentUser.getEmail());
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this server!");
            return "redirect:/admin/servers/" + id;
        }

        if (result.hasErrors()) {
            model.addAttribute("server", updatedServer);
            return "admin/adminEditServer";
        }

        serverFacade.updateServer(existingServer, updatedServer);
        return "redirect:/admin/servers/" + id;
    }


    @GetMapping("/servers/delete/{id}")
    public String deleteServer(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Server server = serverFacade.findById(id);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (server == null || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Server not found or not authenticated.");
            return "redirect:/admin/servers";
        }

        User currentUser = userOpt.get();
        boolean isOwner = server.getCreatedBy().getEmail().equals(currentUser.getEmail());
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        if (isOwner || isAdmin) {
            serverFacade.deleteServer(id);
            return "redirect:/admin/servers";
        }

        redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this server!");
        return "redirect:/admin/servers";
    }


    @PostMapping("/servers/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long id,
                                @PathVariable Long commentId,
                                Authentication authentication) {
        Optional<Comment> optionalComment = commentFacade.findById(commentId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (optionalComment.isPresent() && userOpt.isPresent()) {
            Comment comment = optionalComment.get();
            User currentUser = userOpt.get();

            if (comment.getCreatedBy().getEmail().equals(currentUser.getEmail()) ||
                    "ADMIN".equals(currentUser.getRole())) {
                commentFacade.deleteComment(commentId);
            }
        }

        return "redirect:/admin/servers/" + id;
    }

    @GetMapping("/users")
    public String viewUser(Model model,
                           RedirectAttributes redirectAttributes,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) String email,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) Boolean banned,
                           @RequestParam(required = false) Boolean deleted,
                           @RequestParam(required = false) String startDateRegistration,
                           @RequestParam(required = false) String endDateRegistration,
                           @RequestParam(required = false) String startDateLastLogin,
                           @RequestParam(required = false) String endDateLastLogin,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "id") String sortField,
                           @RequestParam(defaultValue = "asc") String sortDirection) {
        if (startDateRegistration != null && endDateRegistration != null &&
                !startDateRegistration.isEmpty() && !endDateRegistration.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDateRegistration + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDateRegistration + "T23:59:59");
            if (end.isBefore(start)) {
                redirectAttributes.addFlashAttribute("error", "End registration date cannot be earlier than start date.");
                return "redirect:/admin/dashboard";
            }
        }

        if (startDateLastLogin != null && endDateLastLogin != null &&
                !startDateLastLogin.isEmpty() && !endDateLastLogin.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDateLastLogin + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDateLastLogin + "T23:59:59");
            if (end.isBefore(start)) {
                redirectAttributes.addFlashAttribute("error", "End last login date cannot be earlier than start date.");
                return "redirect:/admin/dashboard";
            }
        }


        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        Page<User> userPage = userFacade.findFilteredUsers(name, email, role, banned, deleted, startDateRegistration, endDateRegistration, startDateLastLogin, endDateLastLogin, pageable);

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

        return "admin/adminUserView";
    }

    @GetMapping("/users/{id}/servers")
    public String viewUserServers(@PathVariable Long id,
                                  @RequestParam(required = false) String serverName,
                                  @RequestParam(required = false) String ipAddress,
                                  @RequestParam(required = false) Integer versionId,
                                  @RequestParam(required = false) Integer modeId,
                                  @RequestParam(required = false) Integer minScore,
                                  @RequestParam(required = false) Integer maxScore,
                                  @RequestParam(required = false) Integer statusId,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  @RequestParam(required = false) String updatedStartDate,
                                  @RequestParam(required = false) String updatedEndDate,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "id") String sortField,
                                  @RequestParam(defaultValue = "asc") String sortDirection,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication) {

        Optional<User> authUserOpt = userFacade.getAuthenticatedUser(authentication);
        if (authUserOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }
        User authUser = authUserOpt.get();

        User user = userFacade.findById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Requested user not found.");
            return "redirect:/admin/users";
        }

        if (serverName != null && !serverName.isEmpty() && !serverName.matches("^[a-zA-Z0-9 .\\-]+$")) {
            redirectAttributes.addFlashAttribute("error", "Server name can only contain letters, numbers, spaces, dots, or hyphens.");
            return "redirect:/admin/users/" + id + "/servers";
        }

        if (ipAddress != null && !ipAddress.isEmpty() && !ipAddress.matches("^[a-zA-Z0-9 .\\-]{0,50}$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid IP address or domain.");
            return "redirect:/admin/users/" + id + "/servers";
        }

        if ((minScore != null && minScore < 0) || (maxScore != null && maxScore < 0)) {
            redirectAttributes.addFlashAttribute("error", "Scores must be positive.");
            return "redirect:/admin/users/" + id + "/servers";
        }

        if (minScore != null && maxScore != null && minScore > maxScore) {
            redirectAttributes.addFlashAttribute("error", "Min score cannot be greater than max score.");
            return "redirect:/admin/users/" + id + "/servers";
        }

        LocalDateTime createdAfter = null;
        LocalDateTime createdBefore = null;
        LocalDateTime updatedAfter = null;
        LocalDateTime updatedBefore = null;

        try {
            if (startDate != null && !startDate.isEmpty()) {
                createdAfter = LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                createdBefore = LocalDateTime.parse(endDate);
            }
            if (createdAfter != null && createdBefore != null && createdBefore.isBefore(createdAfter)) {
                redirectAttributes.addFlashAttribute("error", "End date cannot be earlier than start date.");
                return "redirect:/admin/users/" + id + "/servers";
            }

            if (updatedStartDate != null && !updatedStartDate.isEmpty()) {
                updatedAfter = LocalDateTime.parse(updatedStartDate);
            }
            if (updatedEndDate != null && !updatedEndDate.isEmpty()) {
                updatedBefore = LocalDateTime.parse(updatedEndDate);
            }
            if (updatedAfter != null && updatedBefore != null && updatedBefore.isBefore(updatedAfter)) {
                redirectAttributes.addFlashAttribute("error", "Update end date cannot be earlier than start date.");
                return "redirect:/admin/users/" + id + "/servers";
            }
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid date format. Use YYYY-MM-DDThh:mm.");
            return "redirect:/admin/users/" + id + "/servers";
        }

        if (page < 0) page = 0;

        List<String> allowedSortFields = List.of("id", "serverName", "ipAddress", "score");
        if (!allowedSortFields.contains(sortField)) sortField = "id";

        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            sortDirection = "asc";
        }

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        ServerVersion version = null;
        if (versionId != null) {
            ServerVersion[] versions = ServerVersion.values();
            if (versionId >= 0 && versionId < versions.length) version = versions[versionId];
        }

        ServerMode mode = null;
        if (modeId != null) {
            ServerMode[] modes = ServerMode.values();
            if (modeId >= 0 && modeId < modes.length) mode = modes[modeId];
        }

        ServerStatus status = null;
        if (statusId != null) {
            ServerStatus[] statuses = ServerStatus.values();
            if (statusId >= 0 && statusId < statuses.length) status = statuses[statusId];
        }

        Page<Server> serverPage = serverFacade.findFilteredServersForUser(
                user.getId(),
                serverName,
                ipAddress,
                version,
                mode,
                minScore,
                maxScore,
                status,
                createdAfter,
                createdBefore,
                updatedAfter,
                updatedBefore,
                pageable
        );

        model.addAttribute("user", user);
        model.addAttribute("servers", serverPage.getContent());
        model.addAttribute("totalPages", serverPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");

        model.addAttribute("serverName", serverName);
        model.addAttribute("ipAddress", ipAddress);
        model.addAttribute("versionId", versionId);
        model.addAttribute("modeId", modeId);
        model.addAttribute("statusId", statusId);
        model.addAttribute("minScore", minScore);
        model.addAttribute("maxScore", maxScore);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("updatedStartDate", updatedStartDate);
        model.addAttribute("updatedEndDate", updatedEndDate);
        model.addAttribute("versions", ServerVersion.values());
        model.addAttribute("modes", ServerMode.values());
        model.addAttribute("statuses", ServerStatus.values());

        return "admin/adminUserServersView";
    }

    @GetMapping("/users/{userId}/servers/{serverId}")
    public String userServersDetail(@PathVariable("serverId") Long serverId,
                                    @PathVariable("userId") Long userId,
                                    Authentication authentication,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Server server = serverFacade.findById(serverId);
        if (server == null) {
            redirectAttributes.addFlashAttribute("error", "Server not found.");
            return "redirect:/admin/users";
        }

        User user = userFacade.findById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/admin/users";
        }

        model.addAttribute("server", server);
        model.addAttribute("user", user);
        model.addAttribute("comment", new Comment());

        userFacade.getAuthenticatedUser(authentication)
                .ifPresent(authUser -> model.addAttribute("currentUserEmail", authUser.getEmail()));

        return "admin/adminUserServersDetail";
    }


    @GetMapping("/users/{userId}/servers/edit/{serverId}")
    public String editUserServer(@PathVariable Long userId,
                                 @PathVariable Long serverId,
                                 Model model,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = userFacade.findById(userId);
        if(user == null){
            return "redirect:/admin/users/" + userId + "/servers";
        }

        Server server = serverFacade.findById(serverId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        model.addAttribute("server", server);
        model.addAttribute("user", user);
        return "admin/adminEditUserServer";
    }

    @PostMapping("/users/{userId}/servers/edit/{serverId}")
    public String updateUserServer(@PathVariable Long userId,
                                   @PathVariable Long serverId,
                                   @Valid @ModelAttribute("server") Server updatedServer,
                                   BindingResult result,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Server existingServer = serverFacade.findById(serverId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (result.hasErrors()) {
            model.addAttribute("server", updatedServer);
            model.addAttribute("user", userFacade.findById(userId));
            return "admin/adminEditUserServer";
        }

        serverFacade.updateServer(existingServer, updatedServer);
        return "redirect:/admin/users/" + userId + "/servers/" + serverId;
    }


    @GetMapping("/users/{userId}/servers/delete/{serverId}")
    public String deleteUserServer(@PathVariable Long userId,
                                   @PathVariable Long serverId,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        Server server = serverFacade.findById(serverId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (server == null || userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Server not found or not authenticated.");
            return "redirect:/admin/users/" + userId + "/servers";
        }

        User currentUser = userOpt.get();

        boolean isOwner = server.getCreatedBy().getEmail().equals(currentUser.getEmail());
        boolean isAdmin = currentUser.getRole().equalsIgnoreCase("ADMIN");

        if (isOwner || isAdmin) {
            serverFacade.deleteServer(serverId);
            redirectAttributes.addFlashAttribute("success", "Server successfully deleted.");
            return "redirect:/admin/users/" + userId + "/servers";
        }

        redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this server!");
        return "redirect:/admin/users/" + userId + "/servers/" + serverId;
    }

    @GetMapping("/users/{id}")
    public String showUserDetail(@PathVariable Long id, Model model) {
        User user = userFacade.findById(id);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "admin/dashboard";
        }
        model.addAttribute("user", user);
        return "admin/adminUserDetail";
    }

    @GetMapping("/users/advanced/{id}")
    public String advancedUser(@PathVariable Long id, Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes){
        User user = userFacade.findById(id);

        if(user == null){
            return "redirect:/admin/dashboard";
        }

        Optional<User> currentUserOpt = userFacade.getAuthenticatedUser(authentication);

        if(currentUserOpt.isPresent() && user.getEmail().equals(currentUserOpt.get().getEmail())){
            redirectAttributes.addFlashAttribute("error", "You cannot access advanced settings for yourself.");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        return "admin/adminUserAdvanced";
    }

    @PostMapping("/users/advanced/{id}")
    public String updatedAdvancedUser(@PathVariable Long id,
                                      @RequestParam("role") String role,
                                      @RequestParam(value = "ban", required = false) String ban,
                                      Model model,
                                      Authentication authentication){
        User user = userFacade.findById(id);

        Optional<User> currentUserOpt = userFacade.getAuthenticatedUser(authentication);

        if(user == null || (currentUserOpt.isPresent() && user.getEmail().equals(currentUserOpt.get().getEmail()))){
            return "redirect:/admin/dashboard";
        }

        user.setRole(role);
        user.setBanned(ban != null && ban.equals("on"));
        userFacade.update(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id,
                             Authentication authentication) {
        User user = userFacade.findById(id);

        Optional<User> currentUserOpt = userFacade.getAuthenticatedUser(authentication);

        if (user != null && currentUserOpt.isPresent() && !user.getEmail().equals(currentUserOpt.get().getEmail())) {
            userFacade.softDelete(user);
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/user/{userId}/servers/{serverId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long userId,
                                @PathVariable Long serverId,
                                @PathVariable Long commentId,
                                Authentication authentication) {
        Optional<Comment> optionalComment = commentFacade.findById(commentId);
        Optional<User> userOpt = userFacade.getAuthenticatedUser(authentication);

        if (optionalComment.isPresent() && userOpt.isPresent()) {
            Comment comment = optionalComment.get();
            User currentUser = userOpt.get();

            if (comment.getCreatedBy().getId().equals(userId)) {
                if (comment.getCreatedBy().getEmail().equals(currentUser.getEmail()) ||
                        "ADMIN".equals(currentUser.getRole())) {
                    commentFacade.deleteComment(commentId);
                }
            }
        }
        return "redirect:/admin/users/ " + userId + "servers/" + serverId;
    }

}
