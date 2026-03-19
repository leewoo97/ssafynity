package com.ssafynity.demo.controller;

import com.ssafynity.demo.domain.Event;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.service.EventService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("upcomingEvents", eventService.findUpcoming());
        model.addAttribute("ongoingEvents", eventService.findOngoing());
        model.addAttribute("allEvents", eventService.findAll());
        model.addAttribute("eventTypes", eventService.getEventTypes());
        return "events/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id).orElseThrow();
        model.addAttribute("event", event);
        model.addAttribute("otherEvents", eventService.findUpcomingTop4());
        return "events/detail";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        model.addAttribute("event", new Event());
        model.addAttribute("eventTypes", eventService.getEventTypes());
        model.addAttribute("locations", eventService.getLocations());
        return "events/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                         @RequestParam(defaultValue = "ONLINE") String location,
                         @RequestParam(defaultValue = "기타") String eventType,
                         @RequestParam(defaultValue = "0") int maxParticipants,
                         HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) return "redirect:/member/login";
        Event event = eventService.create(title, description, startDate, endDate,
                location, eventType, maxParticipants, member);
        return "redirect:/events/" + event.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Member member = (Member) session.getAttribute("loginMember");
        Event event = eventService.findById(id).orElseThrow();
        if (member == null || !event.getOrganizer().getId().equals(member.getId())) {
            return "redirect:/events/" + id;
        }
        model.addAttribute("event", event);
        model.addAttribute("eventTypes", eventService.getEventTypes());
        model.addAttribute("locations", eventService.getLocations());
        return "events/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam(required = false) String description,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                       @RequestParam(defaultValue = "ONLINE") String location,
                       @RequestParam(defaultValue = "기타") String eventType,
                       @RequestParam(defaultValue = "0") int maxParticipants,
                       HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        Event event = eventService.findById(id).orElseThrow();
        if (member == null || !event.getOrganizer().getId().equals(member.getId())) {
            return "redirect:/events/" + id;
        }
        eventService.update(id, title, description, startDate, endDate, location, eventType, maxParticipants);
        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/join")
    public String join(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("loginMember") == null) return "redirect:/member/login";
        eventService.join(id);
        return "redirect:/events/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        Event event = eventService.findById(id).orElseThrow();
        boolean isOwner = member != null && event.getOrganizer().getId().equals(member.getId());
        boolean isAdmin = member != null && "ADMIN".equals(member.getRole());
        if (!isOwner && !isAdmin) return "redirect:/events/" + id;
        eventService.delete(id);
        return "redirect:/events";
    }
}
