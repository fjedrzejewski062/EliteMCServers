package com.example.elitemcservers.controller;

import com.example.elitemcservers.entity.Server;
import com.example.elitemcservers.enums.ServerMode;
import com.example.elitemcservers.enums.ServerVersion;
import com.example.elitemcservers.facade.ServerFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class HomeController {
    private final ServerFacade serverFacade;

    public HomeController(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String serverName,
                       @RequestParam(required = false) String ipAddress,
                       @RequestParam(required = false) Integer versionId,
                       @RequestParam(required = false) Integer modeId,
                       @RequestParam(required = false) Integer minScore,
                       @RequestParam(required = false) Integer maxScore,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "id") String sortField,
                       @RequestParam(defaultValue = "asc") String sortDirection,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (serverName != null && !serverName.isEmpty()) {
            if (!serverName.matches("^[a-zA-Z0-9 .\\-]+$")) {
                redirectAttributes.addFlashAttribute("error", "Server name can only contain letters, numbers, spaces, dots, or hyphens.");
                return "redirect:/";
            }
        }

        if (ipAddress != null && !ipAddress.isEmpty()) {
            ipAddress = ipAddress.trim();
            if (!ipAddress.matches("^[a-zA-Z0-9 .\\-]{0,50}$")) {
                redirectAttributes.addFlashAttribute("error", "IP address or domain must be up to 50 characters and contain only letters, numbers, spaces, dots, or hyphens.");
                return "redirect:/";
            }
        }

        if (minScore != null && minScore < 0 || maxScore != null && maxScore < 0) {
            redirectAttributes.addFlashAttribute("error", "Scores must be positive.");
            return "redirect:/";
        }

        if (minScore != null && maxScore != null && minScore > maxScore) {
            redirectAttributes.addFlashAttribute("error", "Min score cannot be greater than max score.");
            return "redirect:/";
        }

        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            try {
                LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
                LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
                if (end.isBefore(start)) {
                    redirectAttributes.addFlashAttribute("error", "End date cannot be earlier than start date.");
                    return "redirect:/";
                }
            } catch (DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("error", "Invalid date format. Please use YYYY-MM-DD.");
                return "redirect:/";
            }
        }

        List<String> allowedSortFields = List.of("id", "serverName", "ipAddress", "score");
        if (!allowedSortFields.contains(sortField)) {
            sortField = "id";
        }
        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            sortDirection = "asc";
        }

        if (page < 0) {
            page = 0;
        }

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 10, sort);

        ServerVersion[] versions = ServerVersion.values();
        ServerMode[] modes = ServerMode.values();

        ServerVersion version = (versionId != null && versionId >= 0 && versionId < versions.length) ? versions[versionId] : null;
        ServerMode mode = (modeId != null && modeId >= 0 && modeId < modes.length) ? modes[modeId] : null;

        boolean noFilters = (serverName == null || serverName.isEmpty()) &&
                (ipAddress == null || ipAddress.isEmpty()) &&
                version == null &&
                mode == null &&
                minScore == null &&
                maxScore == null;

        Page<Server> serverPage;

            serverPage = serverFacade.findFilteredServers(
                    null,
                    serverName,
                    ipAddress,
                    version,
                    mode,
                    minScore,
                    maxScore,
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
        model.addAttribute("minScore", minScore);
        model.addAttribute("maxScore", maxScore);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("versions", ServerVersion.values());
        model.addAttribute("modes", ServerMode.values());

        return "home";
    }

}
