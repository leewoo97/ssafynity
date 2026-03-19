package com.ssafynity.demo.service;

import com.ssafynity.demo.domain.Event;
import com.ssafynity.demo.domain.Member;
import com.ssafynity.demo.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private static final String[] EVENT_TYPES = {"스터디", "해커톤", "세미나", "워크숍", "기타"};
    private static final String[] LOCATIONS = {"ONLINE", "OFFLINE", "HYBRID"};

    public String[] getEventTypes() { return EVENT_TYPES; }
    public String[] getLocations()  { return LOCATIONS; }

    @Transactional
    public Event create(String title, String description, LocalDateTime startDate,
                        LocalDateTime endDate, String location, String eventType,
                        int maxParticipants, Member organizer) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .location(location)
                .eventType(eventType)
                .maxParticipants(maxParticipants)
                .organizer(organizer)
                .status("UPCOMING")
                .build();
        return eventRepository.save(event);
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findUpcoming() {
        return eventRepository.findByStartDateAfterOrderByStartDateAsc(LocalDateTime.now());
    }

    public List<Event> findUpcomingTop4() {
        return eventRepository.findTop4ByStartDateAfterOrderByStartDateAsc(LocalDateTime.now());
    }

    public List<Event> findAll() {
        return eventRepository.findAllByOrderByStartDateAsc();
    }

    public List<Event> findOngoing() {
        return eventRepository.findByStatusOrderByStartDateAsc("ONGOING");
    }

    @Transactional
    public void update(Long id, String title, String description, LocalDateTime startDate,
                       LocalDateTime endDate, String location, String eventType, int maxParticipants) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setTitle(title);
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setLocation(location);
        event.setEventType(eventType);
        event.setMaxParticipants(maxParticipants);
    }

    @Transactional
    public void join(Long id) {
        Event event = eventRepository.findById(id).orElseThrow();
        if (event.getMaxParticipants() == 0
                || event.getCurrentParticipants() < event.getMaxParticipants()) {
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);
        }
    }

    @Transactional
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setStatus(status);
    }
}
